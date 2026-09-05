package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;
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

	@GetMapping
	ResponseEntity<PriceResponse> findApplicablePrice(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
			@RequestParam @Positive Long brandId,
			@RequestParam @Positive Long productId) {

		return findApplicablePriceQuery.find(applicationDate, brandId, productId)
				.map(PriceResponse::from)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

}
