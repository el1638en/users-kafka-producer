package com.syscom.service;

import com.syscom.beans.User;
import com.syscom.exceptions.BusinessException;

public interface UserService {

	/**
	 * Création d'un nouvel utilisateur.
	 * 
	 * @param userDTO
	 * @throws BusinessException
	 */
	void create(User user) throws BusinessException;

	/**
	 * Rechercher un utilisateur à partir d'un login.
	 * 
	 * @param login login de l'utilisateur.
	 * @return l'utilisateur.
	 */
	User findByLogin(String login);

	/**
	 * Supprimer un utilisateur.
	 * 
	 * @param login
	 * @throws BusinessException
	 */
	void delete(String login) throws BusinessException;

}
