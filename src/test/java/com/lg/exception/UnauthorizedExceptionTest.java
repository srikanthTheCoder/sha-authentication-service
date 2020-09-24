package com.lg.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import com.lg.enums.ErrorCode;

class UnauthorizedExceptionTest {


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	void test() {
		thrown.expect(UnauthorizedException.class);
		thrown.expectMessage("unauthorised");
		UnauthorizedException obj = new UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
		assertNotNull(obj);
	}
}
