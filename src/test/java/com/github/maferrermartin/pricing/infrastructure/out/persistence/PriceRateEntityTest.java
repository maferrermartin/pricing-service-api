package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class PriceRateEntityTest {

	@Test
	void constructorAssignsEachArgumentToItsMatchingField() {
		var start = LocalDateTime.of(2020, 6, 14, 0, 0);
		var end = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

		var entity = new PriceRateEntity(1L, start, end, 4L, 35455L, 1, new BigDecimal("38.95"), "EUR");

		assertThat(entity.getBrandId()).isEqualTo(1L);
		assertThat(entity.getStartDate()).isEqualTo(start);
		assertThat(entity.getEndDate()).isEqualTo(end);
		assertThat(entity.getPriceList()).isEqualTo(4L);
		assertThat(entity.getProductId()).isEqualTo(35455L);
		assertThat(entity.getPriority()).isEqualTo(1);
		assertThat(entity.getPrice()).isEqualByComparingTo("38.95");
		assertThat(entity.getCurrency()).isEqualTo("EUR");
	}

}
