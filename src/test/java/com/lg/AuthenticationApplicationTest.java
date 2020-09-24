package com.lg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthenticationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationApplicationTest {

	@Test
	void applicationStarts() {
		AuthenticationApplication.main(new String[] { "Hello", "World" });
		assertTrue(true);
	}

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
