package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class RestExceptionHandlerTest {

	private final RestExceptionHandler handler = new RestExceptionHandler();

	@Test
	void missingParameterReturns400MentioningTheParameterName() {
		var ex = new MissingServletRequestParameterException("productId", "Long");

		var response = handler.handleMissingParameter(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).contains("productId");
	}

	@Test
	void dateTypeMismatchReturns400WithTheExpectedFormat() {
		var ex = new MethodArgumentTypeMismatchException(
				"no-es-una-fecha", LocalDateTime.class, "applicationDate", null, null);

		var response = handler.handleTypeMismatch(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).contains("yyyy-MM-ddTHH:mm:ss");
	}

	@Test
	void nonDateTypeMismatchReturns400WithoutTheDateFormatHint() {
		var ex = new MethodArgumentTypeMismatchException("abc", Long.class, "brandId", null, null);

		var response = handler.handleTypeMismatch(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).contains("brandId").doesNotContain("yyyy-MM-dd");
	}

	@Test
	void constraintViolationReturns400WithTheOriginalMessage() {
		var ex = new ConstraintViolationException("findApplicablePrice.brandId: debe ser mayor que 0", Set.of());

		var response = handler.handleConstraintViolation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).contains("debe ser mayor que 0");
	}

	@Test
	void noResourceFoundReturns404InsteadOfTheGenericCatchAll() {
		var ex = new NoResourceFoundException(HttpMethod.GET, "esto-no-existe", "/esto-no-existe");

		var response = handler.handleNoResourceFound(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().message()).contains("esto-no-existe");
	}

	@Test
	void unexpectedExceptionReturns500WithAGenericMessage() {
		var response = handler.handleUnexpected(new RuntimeException("detalle interno que no debe salir"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().message()).isEqualTo("Error, por favor contacte con el administrador");
	}

}
