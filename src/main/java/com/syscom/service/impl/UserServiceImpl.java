package com.syscom.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.syscom.beans.User;
import com.syscom.event.user.UserDeletedEvent;
import com.syscom.event.user.UserUpsertEvent;
import com.syscom.exceptions.BusinessException;
import com.syscom.mapper.event.UserUpsertEventMapper;
import com.syscom.producer.user.UserDeletedProducer;
import com.syscom.producer.user.UserUpsertProducer;
import com.syscom.repository.UserRepository;
import com.syscom.service.UserService;
import org.springframework.util.CollectionUtils;

/**
 * Implémentation du contrat d'interface des services métiers des utilisateurs
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserUpsertEventMapper userUpsertEventMapper;

	@Autowired
	private UserUpsertProducer userUpsertProducer;

	@Autowired
	private UserDeletedProducer userDeletedProducer;

	@Override
	public void create(User user) throws BusinessException {
		logger.info("Create new user {}", user);
		Assert.notNull(user, "User must not be null");
		List<String> errors = validateUser(user);
		if (!errors.isEmpty()) {
			throw new BusinessException(StringUtils.join(errors, ". "));
		}

		if (userRepository.findByLogin(user.getLogin()) != null) {
			throw new BusinessException("Login already used.");
		}
		user = userRepository.save(user);
		sendUserUpsertEvent(user);
	}

	@Override
	public User update(String login, User user) throws BusinessException {
		logger.info("Update user {}", user);
		Assert.notNull(user, "User must not be null");
		List<String> errors = validateUser(user);
		if (!errors.isEmpty()) {
			throw new BusinessException(StringUtils.join(errors, ". "));
		}

		User existUser = userRepository.findByLogin(login);
		if (existUser == null) {
			throw new BusinessException("Unknown user.");
		}

		if (!StringUtils.equals(existUser.getLogin(), login)) {
			throw new BusinessException("Unmatch user !.");
		}

		existUser.setName(user.getName());
		existUser.setFirstName(user.getFirstName());
		existUser.setPassword(user.getPassword());
		existUser.setBirthDay(user.getBirthDay());

		existUser = userRepository.save(existUser);
		sendUserUpsertEvent(existUser);
		return existUser;
	}

	@Override
	public User findByLogin(String login) {
		Assert.notNull(login, "Login must not be null.");
		return userRepository.findByLogin(login);
	}

	@Override
	public void delete(String login) throws BusinessException {
		Assert.notNull(login, "User login are mandatory");
		logger.info("Delete user by login : {}", login);
		if (userRepository.findByLogin(login) == null) {
			throw new BusinessException("Unknown user.");
		}
		userRepository.deleteByLogin(login);
		UserDeletedEvent userDeletedEvent = UserDeletedEvent.builder().login(login).build();
		userDeletedProducer.send(userDeletedEvent.getKey(), userDeletedEvent);
	}

	private void sendUserUpsertEvent(User user) {
		UserUpsertEvent userUpsertEvent = userUpsertEventMapper.beanToEvent(user);
		userUpsertProducer.send(userUpsertEvent.getKey(), userUpsertEvent);
	}

	/**
	 * Vérifier les données obligatoires de l'utilisateur
	 *
	 * @param user Données de l'utilisateur {@link User}
	 * @return Liste de message d'erreurs
	 */
	private List<String> validateUser(User user) {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
		if (!CollectionUtils.isEmpty(constraintViolations)) {
			return constraintViolations.stream()
					.map(violation -> violation.getPropertyPath() + StringUtils.SPACE + violation.getMessage())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
