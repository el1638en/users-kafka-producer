package com.syscom.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.syscom.beans.User;

/**
 * 
 * Repository pour effectuer les CRUD des utilisateurs {@link User}
 *
 */
public interface UserDao extends CrudRepository<User, Long> {

	/**
	 * Rechercher un utilisateur à partir du login.
	 * 
	 * @param login
	 * @return un utilisateur {@link User}
	 */
	@Query("select user from User user where user.login =:login")
	User findByLogin(@Param("login") String login);

	/**
	 * Supprimer un utilisateur à partir de son login.
	 * 
	 * @param login login d'utilisateur.
	 */
	@Modifying
	@Query(name = "deleteByLogin", value = "DELETE FROM User u WHERE u.login =:login")
	void deleteByLogin(@Param("login") String login);

}
