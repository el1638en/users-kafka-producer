package com.syscom.event.user;

import com.syscom.event.AbstractEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserDeletedEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String login;

	@Override
	public String getKey() {
		return this.getClass().getSimpleName();
	}

}
