package com.syscom.exceptions;

/**
 * Classe d'exception pour gérer les erreurs de logique métier.
 *
 */
public class BusinessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BusinessException(final String message) {
		super(message);
	}

}
