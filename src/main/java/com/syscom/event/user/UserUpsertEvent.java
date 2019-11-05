package com.syscom.event.user;

import java.time.LocalDate;

import com.syscom.event.AbstractEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = { "password" })
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = { "password" }, callSuper = false)
public class UserUpsertEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String name;

	protected String firstName;

	protected String login;

	protected String password;

	protected LocalDate birthDay;

	@Override
	public String getKey() {
		return this.getClass().getSimpleName();
	}

}
