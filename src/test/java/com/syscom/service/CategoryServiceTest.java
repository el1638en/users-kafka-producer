package com.syscom.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.syscom.AbstractTest;
import com.syscom.beans.Category;
import com.syscom.event.category.CategoryDeletedEvent;
import com.syscom.event.category.CategoryUpsertEvent;
import com.syscom.exceptions.BusinessException;
import com.syscom.producer.category.CategoryDeletedProducer;
import com.syscom.producer.category.CategoryUpsertProducer;
import com.syscom.repository.CategoryRepository;

public class CategoryServiceTest extends AbstractTest {

	private static final String CODE = "CODE";
	private static final String LIBELLE = "LIBELLE";

	@MockBean
	private CategoryRepository categoryRepository;

	@MockBean
	private CategoryUpsertProducer categoryEventProducer;

	@MockBean
	private CategoryDeletedProducer categoryDeletedProducer;

	@Captor
	private ArgumentCaptor<String> keyCaptor;

	@Captor
	private ArgumentCaptor<CategoryUpsertEvent> categoryEventCaptor;

	@Captor
	private ArgumentCaptor<CategoryDeletedEvent> categoryDeleteEventCaptor;

	@Autowired
	private CategoryService categoryService;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	private Category category;

	@Before
	public void setUp() {
		category = Category.builder().code(CODE).id(1L).libelle(LIBELLE).build();
		Mockito.when(categoryRepository.save(category)).thenReturn(category);
		Mockito.when(categoryRepository.existsCategoryByCode(category.getCode())).thenReturn(true);
	}

	@Test
	public void whenCreateNullCategoryThenThrowException() throws Exception {
		// GIVEN
		exceptionRule.expect(IllegalArgumentException.class);

		// WHEN
		categoryService.create(null);

		// THEN
		verifyNoInteractions(categoryRepository);
		verifyNoInteractions(categoryEventProducer);

	}

	@Test
	public void whenCreateEmptyCategoryThenThrowBusinessException() throws Exception {
		// GIVEN
		exceptionRule.expect(BusinessException.class);

		// WHEN
		categoryService.create(new Category());

		// THEN
		verifyNoInteractions(categoryRepository);
		verifyNoInteractions(categoryEventProducer);
	}

	@Test
	public void whenCreateCategoryWithExistsCodeThenThrowBusinessException() throws Exception {
		// GIVEN
		exceptionRule.expect(BusinessException.class);

		// WHEN
		categoryService.create(category);

		// THEN
		verify(categoryRepository, times(1)).existsCategoryByCode(category.getCode());
	}

	@Test
	public void testCreateCategory() throws Exception {
		// GIVEN
		category.setCode("NEW_CODE");

		// WHEN
		categoryService.create(category);

		// THEN
		verify(categoryRepository, times(1)).save(category);
		verify(categoryEventProducer, times(1)).send(keyCaptor.capture(), categoryEventCaptor.capture());
		assertThat(keyCaptor.getValue()).isEqualTo(CategoryUpsertEvent.class.getSimpleName());
		assertThat(categoryEventCaptor.getValue().getCode()).isEqualTo(category.getCode());
		assertThat(categoryEventCaptor.getValue().getLibelle()).isEqualTo(category.getLibelle());

	}

	@Test
	public void testDeleteCategory() throws Exception {
		// GIVEN
		categoryRepository.save(category);

		// WHEN
		categoryService.delete(CODE);

		// THEN
		verify(categoryRepository, times(1)).deleteByCode((CODE));
		verify(categoryDeletedProducer, times(1)).send(keyCaptor.capture(), categoryDeleteEventCaptor.capture());
		assertThat(keyCaptor.getValue()).isEqualTo(CategoryDeletedEvent.class.getSimpleName());
		assertThat(categoryDeleteEventCaptor.getValue().getCode()).isEqualTo(category.getCode());
	}
}
