package com.syscom.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.syscom.dto.CategoryDTO;
import com.syscom.exceptions.BusinessException;
import com.syscom.mapper.dto.CategoryMapper;
import com.syscom.service.CategoryService;

/**
 * API categorie
 *
 */
@RestController
@RequestMapping(CategoryController.PATH)
public class CategoryController {

	public static final String PATH = "/api/category";

	private final Logger logger = LoggerFactory.getLogger(CategoryController.class);

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CategoryMapper categoryMapper;

	/**
	 * API pour creer une categorie
	 *
	 * @param categoryDTO {@link CategoryDTO}
	 * @throws BusinessException Exception fonctionnelle {@link BusinessException}
	 */
	@PostMapping
	public void createCategory(@RequestBody CategoryDTO categoryDTO) throws BusinessException {
		logger.info("Creation de la categorie : {}", categoryDTO);
		categoryService.create(categoryMapper.dtoToBean(categoryDTO));
	}

}