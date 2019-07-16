package com.syscom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.syscom.AbstractTest;
import com.syscom.beans.User;
import com.syscom.event.user.UserDeletedEvent;
import com.syscom.event.user.UserUpsertEvent;
import com.syscom.exceptions.BusinessException;
import com.syscom.producer.user.UserDeletedProducer;
import com.syscom.producer.user.UserUpsertProducer;
import com.syscom.repository.UserRepository;

public class UserServiceTest extends AbstractTest {

	@Autowired
	private UserRepository userRepository;

	@MockBean
	private UserUpsertProducer userUpsertProducer;

	@MockBean
	private UserDeletedProducer userDeletedProducer;

	@Autowired
	private UserService userService;

	@Captor
	private ArgumentCaptor<String> keyCaptor;

	@Captor
	private ArgumentCaptor<UserUpsertEvent> userUpsertEventCaptor;

	@Captor
	private ArgumentCaptor<UserDeletedEvent> userDeleteEventCaptor;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	private User user;

	@Before
	public void setUp() {
		user = User.builder().firstName(FIRST_NAME).name(NAME).password(PASSWORD).login(LOGIN).birthDay(BIRTH_DAY)
				.build();
	}

	@Test
	public void whenCreateNullUserThenThrowException() throws Exception {
		// GIVEN
		exceptionRule.expect(IllegalArgumentException.class);

		// WHEN
		userService.create(null);

		// THEN
		verifyZeroInteractions(userRepository);
		verifyZeroInteractions(userUpsertProducer);

	}

	@Test
	public void whenCreateEmptyUserThenThrowException() throws Exception {
		// GIVEN
		exceptionRule.expect(BusinessException.class);

		// WHEN
		userService.create(new User());

		// THEN
		verifyZeroInteractions(userRepository);
		verifyZeroInteractions(userUpsertProducer);
	}

	@Test
	public void testCreateUser() throws Exception {
		// GIVEN

		// WHEN
		userService.create(user);

		// THEN
		verify(userUpsertProducer, times(1)).send(keyCaptor.capture(), userUpsertEventCaptor.capture());
		assertThat(keyCaptor.getValue()).isEqualTo(UserUpsertEvent.class.getSimpleName());
		UserUpsertEvent userUpsertEvent = userUpsertEventCaptor.getValue();
		assertThat(userUpsertEvent.getBirthDay()).isEqualTo(user.getBirthDay());
		assertThat(userUpsertEvent.getFirstName()).isEqualTo(user.getFirstName());
		assertThat(userUpsertEvent.getName()).isEqualTo(user.getName());
		assertThat(userUpsertEvent.getLogin()).isEqualTo(user.getLogin());
		assertThat(userUpsertEvent.getPassword()).isEqualTo(user.getPassword());
	}

	@Test
	public void testUpdateUser() throws Exception {
		// GIVEN
		user = userRepository.save(user);
		user.setName("NEW_NAME");
		user.setFirstName("NEW_FIRST_NAME");
		user.setPassword("NEW_PASSWORD");

		// WHEN
		userService.update(LOGIN, user);

		// THEN
		verify(userUpsertProducer, times(1)).send(keyCaptor.capture(), userUpsertEventCaptor.capture());
		assertThat(keyCaptor.getValue()).isEqualTo(UserUpsertEvent.class.getSimpleName());
		UserUpsertEvent userUpsertEvent = userUpsertEventCaptor.getValue();
		assertThat(userUpsertEvent.getName()).isEqualTo("NEW_NAME");
		assertThat(userUpsertEvent.getFirstName()).isEqualTo("NEW_FIRST_NAME");
		assertThat(userUpsertEvent.getLogin()).isEqualTo(user.getLogin());
		assertThat(userUpsertEvent.getPassword()).isEqualTo("NEW_PASSWORD");
		assertThat(userUpsertEvent.getBirthDay()).isEqualTo(user.getBirthDay());
	}

	@Test
	public void testDeleteUser() throws Exception {
		// GIVEN
		user = userRepository.save(user);

		// WHEN
		userService.delete(LOGIN);

		// THEN
		verify(userDeletedProducer, times(1)).send(keyCaptor.capture(), userDeleteEventCaptor.capture());
		UserDeletedEvent userDeleteEvent = userDeleteEventCaptor.getValue();
		assertThat(userDeleteEvent.getLogin()).isEqualTo(LOGIN);
	}
}
