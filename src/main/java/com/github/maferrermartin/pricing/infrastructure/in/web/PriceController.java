package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.maferrermartin.pricing.application.port.in.FindApplicablePriceQuery;

@RestController
@RequestMapping("/api/v1/prices")
@Validated
class PriceController {

	private final FindApplicablePriceQuery findApplicablePriceQuery;

	PriceController(FindApplicablePriceQuery findApplicablePriceQuery) {
		this.findApplicablePriceQuery = findApplicablePriceQuery;
	}

	@Operation(
			summary = "openapi.prices.summary",
			description = "openapi.prices.description")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "openapi.prices.response.200",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PriceResponse.class))),
			@ApiResponse(responseCode = "400", description = "openapi.prices.response.400",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "openapi.prices.response.404",
					content = @Content) })
	@GetMapping
	ResponseEntity<PriceResponse> findApplicablePrice(
			@Parameter(description = "openapi.prices.param.applicationDate", example = "2020-06-14T10:00:00")
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
			@Parameter(description = "openapi.prices.param.brandId", example = "1")
			@RequestParam @Positive Long brandId,
			@Parameter(description = "openapi.prices.param.productId", example = "35455")
			@RequestParam @Positive Long productId) {

		return findApplicablePriceQuery.find(applicationDate, brandId, productId)
				.map(PriceResponse::from)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

}
