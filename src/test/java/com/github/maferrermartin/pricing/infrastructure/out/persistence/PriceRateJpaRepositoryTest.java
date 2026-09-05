package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Limit;

@DataJpaTest
class PriceRateJpaRepositoryTest {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;

	@Autowired
	private PriceRateJpaRepository repository;

	@Test
	void picksTheHighestPriorityCandidateWhenRangesOverlap() {
		var candidates = repository.findApplicableOrderedByPriority(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 14, 16, 0), Limit.of(1));

		assertThat(candidates).hasSize(1);
		assertThat(candidates.getFirst().getPriceList()).isEqualTo(2L);
	}

	@Test
	void fallsBackToTheBaseRateWhenTheOverlappingWindowHasEnded() {
		var candidates = repository.findApplicableOrderedByPriority(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 14, 21, 0), Limit.of(1));

		assertThat(candidates).hasSize(1);
		assertThat(candidates.getFirst().getPriceList()).isEqualTo(1L);
	}

	@Test
	void returnsNoCandidatesWhenNoRangeCoversTheDate() {
		var candidates = repository.findApplicableOrderedByPriority(
				BRAND_ID, PRODUCT_ID, LocalDateTime.of(2020, 6, 13, 10, 0), Limit.of(1));

		assertThat(candidates).isEmpty();
	}

}
