package com.syscom.event;

import java.io.Serializable;

public abstract class AbstractEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract String getKey();

}
