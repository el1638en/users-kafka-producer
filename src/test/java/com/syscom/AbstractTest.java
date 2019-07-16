package com.syscom;

import java.time.LocalDate;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.syscom.beans.User;
import com.syscom.config.TestConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { TestConfiguration.class })
@TestPropertySource(locations = "classpath:application-test.yml")
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@ActiveProfiles("test")
@Transactional
public abstract class AbstractTest {

	// Donnees de test pour les utilisateurs
	protected static final String LOGIN = "LOGIN";
	protected static final String NAME = "NAME";
	protected static final String FIRST_NAME = "FIRST_NAME";
	protected static final String PASSWORD = "PASSWORD";
	protected static final LocalDate BIRTH_DAY = LocalDate.now().minusDays(1);

	protected User getUser() {
		return User.builder().name(NAME).firstName(FIRST_NAME).login(LOGIN).password(PASSWORD).birthDay(BIRTH_DAY)
				.build();

	}

}
