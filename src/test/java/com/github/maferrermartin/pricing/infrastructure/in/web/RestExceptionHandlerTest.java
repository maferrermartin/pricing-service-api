package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class RestExceptionHandlerTest {

	private RestExceptionHandler handler;

	@BeforeEach
	void setUp() {
		var source = new ResourceBundleMessageSource();
		source.setBasename("messages");
		source.setDefaultEncoding("UTF-8");
		handler = new RestExceptionHandler(source);
		LocaleContextHolder.setLocale(Locale.of("es"));
	}

	@AfterEach
	void tearDown() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void missingParameterReturns400MentioningTheParameterName() {
		var ex = new MissingServletRequestParameterException("productId", "Long");

		var response = handler.handleMissingParameter(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).contains("productId");
	}

	@Test
	void dateTypeMismatchReturns400WithTheExpectedFormat() {
		var ex = new MethodArgumentTypeMismatchException(
				"no-es-una-fecha", LocalDateTime.class, "applicationDate", methodParameter(0), null);

		var response = handler.handleTypeMismatch(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).contains("yyyy-MM-ddTHH:mm:ss");
	}

	@Test
	void nonDateTypeMismatchReturns400WithoutTheDateFormatHint() {
		var ex = new MethodArgumentTypeMismatchException("abc", Long.class, "brandId", methodParameter(1), null);

		var response = handler.handleTypeMismatch(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).contains("brandId").doesNotContain("yyyy-MM-dd");
	}

	@Test
	void constraintViolationReturns400WithTheOriginalMessage() {
		var ex = new ConstraintViolationException("findApplicablePrice.brandId: debe ser mayor que 0", Set.of());

		var response = handler.handleConstraintViolation(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).contains("debe ser mayor que 0");
	}

	@Test
	void noResourceFoundReturns404InsteadOfTheGenericCatchAll() {
		var ex = new NoResourceFoundException(HttpMethod.GET, "esto-no-existe", "/esto-no-existe");

		var response = handler.handleNoResourceFound(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).contains("esto-no-existe");
	}

	@Test
	void unexpectedExceptionReturns500WithAGenericMessageInSpanishByDefault() {
		var response = handler.handleUnexpected(new RuntimeException("detalle interno que no debe salir"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assert response.getBody() != null;
        assertThat(response.getBody().message()).isEqualTo("Error, por favor contacte con el administrador");
	}

	@Test
	void resolvesMessagesInEnglishWhenTheLocaleIsEnglish() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		var response = handler.handleMissingParameter(new MissingServletRequestParameterException("productId", "Long"));

        assert response.getBody() != null;
        assertThat(response.getBody().message()).isEqualTo("Missing required parameter 'productId'");
	}

	@Test
	void fallsBackToSpanishWhenTheLocaleHasNoDedicatedFile() {
		LocaleContextHolder.setLocale(Locale.FRENCH);

		var response = handler.handleUnexpected(new RuntimeException("boom"));

        assert response.getBody() != null;
        assertThat(response.getBody().message()).isEqualTo("Error, por favor contacte con el administrador");
	}

	private static MethodParameter methodParameter(int index) {
		try {
			Method method = PriceController.class.getDeclaredMethod(
					"findApplicablePrice", LocalDateTime.class, Long.class, Long.class);
			return new MethodParameter(method, index);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

}
