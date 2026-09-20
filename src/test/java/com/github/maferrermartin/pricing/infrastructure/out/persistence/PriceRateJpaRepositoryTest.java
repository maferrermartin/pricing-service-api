package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;

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
	void returnsEveryCandidateWhoseRangeCoversTheDateIncludingOverlappingOnes() {
		var candidates = repository.findApplicable(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 14, 16, 0));

		assertThat(candidates)
				.extracting(PriceRateEntity::getPriceList, PriceRateEntity::getPriority)
				.containsExactlyInAnyOrder(tuple(1L, 0), tuple(2L, 1));
	}

	@Test
	void returnsOnlyTheBaseRateWhenTheOverlappingWindowHasEnded() {
		var candidates = repository.findApplicable(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 14, 21, 0));

		assertThat(candidates).extracting(PriceRateEntity::getPriceList).containsExactly(1L);
	}

	@Test
	void returnsNoCandidatesWhenNoRangeCoversTheDate() {
		var candidates = repository.findApplicable(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 13, 10, 0));

		assertThat(candidates).isEmpty();
	}

}
