package com.syscom.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.syscom.beans.Category;
import com.syscom.event.category.CategoryDeletedEvent;
import com.syscom.event.category.CategoryUpsertEvent;
import com.syscom.exceptions.BusinessException;
import com.syscom.mapper.event.CategoryUpsertEventMapper;
import com.syscom.producer.category.CategoryDeletedProducer;
import com.syscom.producer.category.CategoryUpsertProducer;
import com.syscom.repository.CategoryRepository;
import com.syscom.service.CategoryService;
import org.springframework.util.CollectionUtils;

/**
 * Implémentation du contrat d'interface des services métiers des catégories
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CategoryServiceImpl implements CategoryService {

	private final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryUpsertEventMapper categoryEventMapper;

	@Autowired
	private CategoryUpsertProducer categoryCreatedProducer;

	@Autowired
	private CategoryDeletedProducer categoryDeletedProducer;

	@Override
	public void create(Category category) throws BusinessException {
		logger.info("Create new category {}", category);
		Assert.notNull(category, "Category must not be null");
		List<String> errors = validateCategory(category);
		if (!errors.isEmpty()) {
			throw new BusinessException(StringUtils.join(errors, ". "));
		}

		if (categoryRepository.existsCategoryByCode(category.getCode())) {
			throw new BusinessException("Code category already used.");
		}
		categoryRepository.save(category);
		CategoryUpsertEvent categoryEvent = categoryEventMapper.beanToEvent(category);
		categoryCreatedProducer.send(categoryEvent.getKey(), categoryEvent);
	}

	@Override
	public void delete(String code) throws BusinessException {
		Assert.notNull(code, "Category code are mandatory");
		logger.info("Delete category by code : {}", code);

		if (!categoryRepository.existsCategoryByCode(code)) {
			throw new BusinessException("Unknown Category");
		}
		categoryRepository.deleteByCode(code);
		CategoryDeletedEvent categoyDeletedEvent = CategoryDeletedEvent.builder().code(code).build();
		categoryDeletedProducer.send(categoyDeletedEvent.getKey(), categoyDeletedEvent);

	}

	/**
	 * Vérifier les données obligatoires de la catégorie
	 *
	 * @param category Données de la catégorie {@link Category}
	 * @return Liste de message d'erreurs
	 */
	private List<String> validateCategory(Category category) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Category>> constraintViolations = validator.validate(category);
		if (!CollectionUtils.isEmpty(constraintViolations)) {
			return constraintViolations.stream()
					.map(violation -> violation.getPropertyPath() + StringUtils.SPACE + violation.getMessage())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
