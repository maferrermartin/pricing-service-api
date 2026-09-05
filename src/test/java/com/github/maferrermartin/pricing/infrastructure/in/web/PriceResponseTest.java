package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

class PriceResponseTest {

	@Test
	void mapsAllFieldsFromTheDomainValueObjectUsingTheIsoCurrencyCode() {
		var start = LocalDateTime.of(2020, 6, 14, 0, 0);
		var end = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
		var applicablePrice = new ApplicablePrice(
				35455L, 1L, 1L, start, end, new BigDecimal("35.50"), Currency.getInstance("EUR"));

		var response = PriceResponse.from(applicablePrice);

		assertThat(response).isEqualTo(new PriceResponse(35455L, 1L, 1L, start, end, new BigDecimal("35.50"), "EUR"));
	}

}
