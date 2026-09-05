package com.github.maferrermartin.pricing.infrastructure.in.web;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
		return badRequest("Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		if (ex.getRequiredType() == LocalDateTime.class) {
			return badRequest("El parámetro '" + ex.getName() + "' tiene un formato de fecha inválido: '"
					+ ex.getValue() + "'. Formato esperado: yyyy-MM-ddTHH:mm:ss, p.ej. 2020-06-14T10:00:00");
		}
		return badRequest("El parámetro '" + ex.getName() + "' tiene un formato inválido: '" + ex.getValue() + "'");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
		return badRequest("Parámetros de consulta inválidos: " + ex.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(HttpStatus.NOT_FOUND, "Recurso no encontrado: '" + ex.getResourcePath() + "'"));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Error no controlado procesando la petición", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
						"Error, por favor contacte con el administrador"));
	}

	private ResponseEntity<ErrorResponse> badRequest(String message) {
		return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
	}

}
