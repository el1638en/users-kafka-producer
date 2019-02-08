package com.syscom.service.impl;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.syscom.beans.User;
import com.syscom.dao.UserDao;
import com.syscom.exceptions.BusinessException;
import com.syscom.mapper.UserEventMapper;
import com.syscom.producer.UserProducer;
import com.syscom.service.UserService;

/**
 * Implémentation du contrat d'interface des services métiers des utilisateurs
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

	private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserEventMapper userEventMapper;
	
	@Autowired
	private UserProducer userProducer;

	@Override
	public void create(User user) throws BusinessException {
		logger.info("Creation d'un nouvel utilisateur {}", user);
		Assert.notNull(user, "User must not be null");
		List<String> errors = checkUserData(user);
		if (!errors.isEmpty()) {
			throw new BusinessException(StringUtils.join(errors, " "));
		}
		String login = upperCase(user.getLogin());
		if (userDao.findByLogin(login) != null) {
			throw new BusinessException("Login already used.");
		}
		
		user = User.builder().login(login).firstName(user.getFirstName()).name(user.getName())
				.password(user.getPassword()).build();
		userDao.save(user);
		userProducer.send(userEventMapper.beanToEvent(user));
	}

	@Override
	public User findByLogin(String login) {
		Assert.notNull(login, "Login must not be null.");
		return userDao.findByLogin(upperCase(login));
	}

	/**
	 * Vérifier les données obligatoires de l'utilisateur
	 *
	 * @param user Données de l'utilisateur {@link User}
	 * @return Liste de message d'erreurs
	 */
	private List<String> checkUserData(User user) {
		List<String> errors = new ArrayList<>();
		if (isEmpty(user.getName())) {
			errors.add("User name are mandatory");
		}
		if (isEmpty(user.getFirstName())) {
			errors.add("User first name are mandatory");
		}
		if (isEmpty(user.getLogin())) {
			errors.add("User login are mandatory");
		}
		if (isEmpty(user.getPassword())) {
			errors.add("User password are mandatory");
		}
		return errors;
	}

	@Override
	public void delete(String login) throws BusinessException {
		logger.info("Suppression de l'utilisateur ayant pour login : {}", login);
		Assert.notNull(login, "User login are mandatory");
		String upperLogin = upperCase(login);
		if (userDao.findByLogin(upperLogin) == null) {
			throw new BusinessException("Unknown user");
		}
		userDao.deleteByLogin(upperLogin);
	}

}
