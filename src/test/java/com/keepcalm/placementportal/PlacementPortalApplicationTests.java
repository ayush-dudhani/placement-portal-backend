package com.keepcalm.placementportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.mockito.Mockito;

import com.keepcalm.placementportal.repository.UserRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class PlacementPortalApplicationTests {

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public UserRepository userRepository() {
			// return a Mockito mock instance to satisfy injection points in the context
			return Mockito.mock(UserRepository.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
