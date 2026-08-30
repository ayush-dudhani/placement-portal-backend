package com.keepcalm.placementportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@EnableCaching
public class PlacementPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlacementPortalApplication.class, args);
	}

}
