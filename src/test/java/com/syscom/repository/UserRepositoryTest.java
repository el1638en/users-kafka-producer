package com.syscom.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.AbstractTest;
import com.syscom.beans.User;

public class UserRepositoryTest extends AbstractTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testFindByLogin() {
		// GIVEN
		userRepository.save(getUser());

		// WHEN
		User findUser = userRepository.findByLogin(getUser().getLogin());

		// THEN
		assertThat(findUser).isNotNull();
		assertThat(findUser.getLogin()).isEqualTo(LOGIN);
		assertThat(findUser.getFirstName()).isEqualTo(FIRST_NAME);
		assertThat(findUser.getName()).isEqualTo(NAME);
		assertThat(findUser.getId()).isNotNull();
	}

	@Test
	public void testDeleteByLogin() {
		// GIVEN
		userRepository.save(getUser());

		// WHEN
		userRepository.deleteByLogin(LOGIN);
		User findUser = userRepository.findByLogin(LOGIN);

		// THEN
		assertThat(findUser).isNull();
	}

}
