package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PriceRateJpaRepositoryTest {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;

	@Autowired
	private PriceRateJpaRepository repository;

	@Test
	void returnsEveryRateOfTheBrandAndProductRegardlessOfItsDateRange() {
		var candidates = repository.findByBrandIdAndProductId(BRAND_ID, PRODUCT_ID);

		assertThat(candidates).extracting(PriceRateEntity::getPriceList)
				.containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
	}

	@Test
	void returnsAnEmptyListWhenTheBrandAndProductHaveNoRates() {
		var candidates = repository.findByBrandIdAndProductId(999L, 999L);

		assertThat(candidates).isEmpty();
	}

}
