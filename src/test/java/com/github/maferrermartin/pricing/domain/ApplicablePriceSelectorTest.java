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
	private static final Currency EUR = Currency.getInstance("EUR");

	private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0);
	private static final LocalDateTime COVERING_START = LocalDateTime.of(2020, 6, 14, 0, 0);
	private static final LocalDateTime COVERING_END = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

	@Test
	void returnsEmptyWhenThereAreNoCandidates() {
		var result = ApplicablePriceSelector.select(List.of(), APPLICATION_DATE);

		assertThat(result).isEmpty();
	}

	@Test
	void returnsTheOnlyCandidateWhenItCoversTheDate() {
		var onlyCandidate = candidateWith(1L, 0, new BigDecimal("35.50"), COVERING_START, COVERING_END);

		var result = ApplicablePriceSelector.select(List.of(onlyCandidate), APPLICATION_DATE);

		assertThat(result).contains(onlyCandidate);
	}

	@Test
	void ignoresACandidateThatStartsAfterTheApplicationDate() {
		var future = candidateWith(1L, 0, new BigDecimal("35.50"),
				APPLICATION_DATE.plusSeconds(1), COVERING_END);

		var result = ApplicablePriceSelector.select(List.of(future), APPLICATION_DATE);

		assertThat(result).isEmpty();
	}

	@Test
	void ignoresACandidateThatEndedBeforeTheApplicationDate() {
		var expired = candidateWith(1L, 0, new BigDecimal("35.50"),
				COVERING_START, APPLICATION_DATE.minusSeconds(1));

		var result = ApplicablePriceSelector.select(List.of(expired), APPLICATION_DATE);

		assertThat(result).isEmpty();
	}

	@Test
	void includesACandidateWhoseRangeStartsExactlyOnTheApplicationDate() {
		var startsNow = candidateWith(1L, 0, new BigDecimal("35.50"), APPLICATION_DATE, COVERING_END);

		var result = ApplicablePriceSelector.select(List.of(startsNow), APPLICATION_DATE);

		assertThat(result).contains(startsNow);
	}

	@Test
	void includesACandidateWhoseRangeEndsExactlyOnTheApplicationDate() {
		var endsNow = candidateWith(1L, 0, new BigDecimal("35.50"), COVERING_START, APPLICATION_DATE);

		var result = ApplicablePriceSelector.select(List.of(endsNow), APPLICATION_DATE);

		assertThat(result).contains(endsNow);
	}

	@Test
	void selectsTheHighestPriorityAmongCandidatesThatCoverTheDate() {
		var basePrice = candidateWith(1L, 0, new BigDecimal("35.50"), COVERING_START, COVERING_END);
		var higherPriority = candidateWith(2L, 1, new BigDecimal("25.45"), COVERING_START, COVERING_END);

		var result = ApplicablePriceSelector.select(List.of(basePrice, higherPriority), APPLICATION_DATE);

		assertThat(result).contains(higherPriority);
	}

	@Test
	void ignoresAHigherPriorityCandidateThatDoesNotCoverTheDate() {
		var basePrice = candidateWith(1L, 0, new BigDecimal("35.50"), COVERING_START, COVERING_END);
		var expiredHigherPriority = candidateWith(2L, 1, new BigDecimal("25.45"),
				COVERING_START, APPLICATION_DATE.minusSeconds(1));

		var result = ApplicablePriceSelector.select(List.of(basePrice, expiredHigherPriority), APPLICATION_DATE);

		assertThat(result).contains(basePrice);
	}

	@Test
	void breaksAPriorityTieByPickingTheLowestPriceList() {
		var priceListThree = candidateWith(3L, 1, new BigDecimal("30.50"), COVERING_START, COVERING_END);
		var priceListTwo = candidateWith(2L, 1, new BigDecimal("25.45"), COVERING_START, COVERING_END);

		var result = ApplicablePriceSelector.select(List.of(priceListThree, priceListTwo), APPLICATION_DATE);

		assertThat(result).contains(priceListTwo);
	}

	private ApplicablePrice candidateWith(Long priceList, int priority, BigDecimal price,
			LocalDateTime start, LocalDateTime end) {
		return new ApplicablePrice(PRODUCT_ID, BRAND_ID, priceList, priority, start, end, price, EUR);
	}

}
