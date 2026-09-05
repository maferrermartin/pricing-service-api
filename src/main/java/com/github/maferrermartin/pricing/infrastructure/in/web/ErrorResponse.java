package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;

record ErrorResponse(
		@Schema(description = "openapi.schema.error-response.timestamp") LocalDateTime timestamp,
		@Schema(description = "openapi.schema.error-response.status", example = "400") int status,
		@Schema(description = "openapi.schema.error-response.error", example = "Bad Request") String error,
		@Schema(description = "openapi.schema.error-response.message") String message) {

	static ErrorResponse of(HttpStatus status, String message) {
		return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
	}

}
