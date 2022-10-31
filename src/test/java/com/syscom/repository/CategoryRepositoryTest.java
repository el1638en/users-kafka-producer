package com.syscom.repository;

import com.syscom.AbstractTest;
import com.syscom.beans.Category;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoryRepositoryTest extends AbstractTest {

	private static final String CODE = "CODE";
	private static final String LIBELLE = "LIBELLE";

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	public void testExistsCategoryByCode() {
		// GIVEN
		Category category = Category.builder().code(CODE).libelle(LIBELLE).build();
		categoryRepository.save(category);

		// WHEN
		boolean exists = categoryRepository.existsCategoryByCode(CODE);

		// THEN
		assertThat(exists).isTrue();

	}

	@Test
	public void testExistsCategoryByWrongCode() {
		// GIVEN
		Category category = Category.builder().code(CODE).libelle(LIBELLE).build();
		categoryRepository.save(category);

		// WHEN
		boolean exists = categoryRepository.existsCategoryByCode("WRONG_CODE");

		// THEN
		assertThat(exists).isFalse();

	}

}
