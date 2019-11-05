package com.syscom.service;

import com.syscom.beans.Category;
import com.syscom.exceptions.BusinessException;

public interface CategoryService {

	/**
	 * Création d'une nouvelle categorie.
	 * 
	 * @param category
	 * @throws BusinessException
	 */
	void create(Category category) throws BusinessException;

	/**
	 * Supprimer une categorie.
	 * 
	 * @param code
	 * @throws BusinessException
	 */
	void delete(String code) throws BusinessException;

}
