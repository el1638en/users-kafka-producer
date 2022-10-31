package com.syscom.rest;

import com.syscom.beans.User;
import com.syscom.dto.UserDTO;
import com.syscom.mapper.dto.UserMapper;
import com.syscom.producer.user.UserDeletedProducer;
import com.syscom.producer.user.UserUpsertProducer;
import com.syscom.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIntTest extends AbstractIntTest {

	private UserDTO userDTO;

	@MockBean
	private UserUpsertProducer userCreatedProducer;

	@MockBean
	private UserDeletedProducer userDeletedProducer;

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	@Before
	public void setup() {
		userDTO = UserDTO.builder().login(LOGIN).password(PASSWORD).name(NAME).firstName(FIRST_NAME).birthDay(BIRTH_DAY)
				.build();
	}

	@Test
	public void testCreateEmptyUser() throws Exception {
		// GIVEN

		// WHEN

		// THEN
		mockMvc.perform(MockMvcRequestBuilders.post(UserController.PATH).contentType(APPLICATION_JSON_UTF8)
				.content(convertObjectToJsonBytes(new UserDTO()))).andExpect(status().is4xxClientError());
	}

	@Test
	public void testCreateUser() throws Exception {
		// GIVEN

		// WHEN

		// THEN
		mockMvc.perform(MockMvcRequestBuilders.post(UserController.PATH).contentType(APPLICATION_JSON_UTF8)
				.content(convertObjectToJsonBytes(userDTO))).andExpect(status().isOk());
	}

	@Test
	public void testUpdateUser() throws Exception {
		// GIVEN
		User user = User.builder().name(NAME).firstName(FIRST_NAME).login(LOGIN).password(PASSWORD).birthDay(BIRTH_DAY)
				.build();
		userService.create(user);
		user.setName("NEW_NAME");
		user.setFirstName("NEW_FIRST_NAME");

		// WHEN

		// THEN
		mockMvc.perform(MockMvcRequestBuilders.put(UserController.PATH + "/" + LOGIN).contentType(APPLICATION_JSON_UTF8)
				.content(convertObjectToJsonBytes(userMapper.beanToDto(user)))).andExpect(status().isOk());
	}

	@Test
	public void testDeleteUser() throws Exception {
		// GIVEN
		User user = User.builder().name(NAME).firstName(FIRST_NAME).login(LOGIN).password(PASSWORD).birthDay(BIRTH_DAY)
				.build();
		userService.create(user);

		// WHEN

		// THEN
		mockMvc.perform(
				MockMvcRequestBuilders.delete(UserController.PATH + "/" + LOGIN).contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk());
	}
}
