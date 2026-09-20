package com.github.maferrermartin.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

class ApplicablePriceSelectorTest {

	private static final Long PRODUCT_ID = 35455L;
	private static final Long BRAND_ID = 1L;
	private static final LocalDateTime START = LocalDateTime.of(2020, 6, 14, 0, 0);
	private static final LocalDateTime END = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
	private static final Currency EUR = Currency.getInstance("EUR");

	@Test
	void returnsEmptyWhenThereAreNoCandidates() {
		var result = ApplicablePriceSelector.highestPriority(List.of());

		assertThat(result).isEmpty();
	}

	@Test
	void returnsTheOnlyCandidateWhenThereIsJustOne() {
		var onlyCandidate = candidateWith(1L, 0, new BigDecimal("35.50"));

		var result = ApplicablePriceSelector.highestPriority(List.of(onlyCandidate));

		assertThat(result).contains(onlyCandidate);
	}

	@Test
	void selectsTheCandidateWithTheHighestPriorityAmongOverlappingOnes() {
		var basePrice = candidateWith(1L, 0, new BigDecimal("35.50"));
		var higherPriority = candidateWith(2L, 1, new BigDecimal("25.45"));

		var result = ApplicablePriceSelector.highestPriority(List.of(basePrice, higherPriority));

		assertThat(result).contains(higherPriority);
	}

	@Test
	void breaksAPriorityTieByPickingTheLowestPriceList() {
		var priceListThree = candidateWith(3L, 1, new BigDecimal("30.50"));
		var priceListTwo = candidateWith(2L, 1, new BigDecimal("25.45"));

		var result = ApplicablePriceSelector.highestPriority(List.of(priceListThree, priceListTwo));

		assertThat(result).contains(priceListTwo);
	}

	private ApplicablePrice candidateWith(Long priceList, int priority, BigDecimal price) {
		return new ApplicablePrice(PRODUCT_ID, BRAND_ID, priceList, priority, START, END, price, EUR);
	}

}
