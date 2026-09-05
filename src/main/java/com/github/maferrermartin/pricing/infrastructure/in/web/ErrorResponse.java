package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

record ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {

	static ErrorResponse of(HttpStatus status, String message) {
		return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
	}

}
