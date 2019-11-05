package com.syscom.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.syscom.beans.Category;
import com.syscom.dto.CategoryDTO;
import com.syscom.producer.category.CategoryUpsertProducer;
import com.syscom.producer.category.CategoryDeletedProducer;

public class CategoryControllerIntTest extends AbstractIntTest {

	private CategoryDTO categoryDTO;

	@MockBean
	private CategoryUpsertProducer categoryCreatedProducer;

	@MockBean
	private CategoryDeletedProducer categoryDeletedProducer;

	@Before
	public void setup() {
		categoryDTO = CategoryDTO.builder().code("CODE").libelle("LIBELLE").build();
	}

	@Test
	public void testCreateEmptyCategory() throws Exception {
		// GIVEN

		// WHEN

		// THEN
		mockMvc.perform(MockMvcRequestBuilders.post(CategoryController.PATH).contentType(APPLICATION_JSON_UTF8)
				.content(convertObjectToJsonBytes(new Category()))).andExpect(status().is4xxClientError());
	}

	@Test
	public void testCreateCategory() throws Exception {
		// GIVEN

		// WHEN

		// THEN
		mockMvc.perform(MockMvcRequestBuilders.post(CategoryController.PATH).contentType(APPLICATION_JSON_UTF8)
				.content(convertObjectToJsonBytes(categoryDTO))).andExpect(status().isOk());
	}

}
