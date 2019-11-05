package com.syscom.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.syscom.dto.UserDTO;
import com.syscom.exceptions.BusinessException;
import com.syscom.mapper.dto.UserMapper;
import com.syscom.service.UserService;

/**
 * API utilisateurs
 *
 */
@RestController
@RequestMapping(UserController.PATH)
public class UserController {

	public static final String PATH = "/api/user";

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	/**
	 * API pour creer un nouvel utilisateur
	 *
	 * @param userDTO {@link UserDTO}
	 * @throws BusinessException Exception fonctionnelle {@link BusinessException}
	 */
	@PostMapping
	public void createUser(@RequestBody UserDTO userDTO) throws BusinessException {
		logger.info("Creation de l'utilisateur : {}", userDTO);
		userService.create(userMapper.dtoToBean(userDTO));
	}

	/**
	 * API pour modifier un utilisateur.
	 *
	 * @param login   loogin de l'utilisateur
	 * @param userDTO {@link UserDTO}
	 * @throws BusinessException Exception fonctionnelle {@link BusinessException}
	 */
	@PutMapping(value = "/{login}")
	public UserDTO update(@PathVariable("login") String login, @RequestBody UserDTO userDTO) throws BusinessException {
		logger.info("Update user {} identified by login {}.", userDTO, login);
		return userMapper.beanToDto(userService.update(toUpperCase(login), userMapper.dtoToBean(userDTO)));
	}

	/**
	 * API pour supprimer un utilisateur.
	 *
	 * @param login identifiant de l'utilisateur à supprimer.
	 * @throws BusinessException Exception fonctionnelle {@link BusinessException}
	 */
	@DeleteMapping(value = "/{login}")
	public void delete(@PathVariable("login") String login) throws BusinessException {
		logger.info("Suppression de l'utilisateur ayant pour login : {}.", toUpperCase(login));
		userService.delete(login);
	}

	private String toUpperCase(String login) {
		return StringUtils.upperCase(login);
	}

}