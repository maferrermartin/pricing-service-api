package com.github.maferrermartin.pricing.infrastructure.in.web;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
class RestExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

	private final MessageSource messageSource;

	RestExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
		return badRequest("error.missing-parameter", ex.getParameterName());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		if (ex.getRequiredType() == LocalDateTime.class) {
			return badRequest("error.invalid-date-format", ex.getName(), ex.getValue());
		}
		return badRequest("error.invalid-parameter-format", ex.getName(), ex.getValue());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
		return badRequest("error.invalid-query-parameters", ex.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(HttpStatus.NOT_FOUND, message("error.resource-not-found", ex.getResourcePath())));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Error no controlado procesando la petición", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, message("error.internal")));
	}

	private ResponseEntity<ErrorResponse> badRequest(String messageCode, Object... args) {
		return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message(messageCode, args)));
	}

	private String message(String code, Object... args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

}
