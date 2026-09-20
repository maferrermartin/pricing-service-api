package com.github.maferrermartin.pricing.domain;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

/**
 * Resolves the applicable price among a product's candidates: keeps only those whose
 * range covers the given date, then picks the highest priority; on a tie, the lowest
 * price list.
 */
public final class ApplicablePriceSelector {

	private static final Comparator<ApplicablePrice> BY_PRIORITY_THEN_LOWEST_PRICE_LIST =
			Comparator.comparing(ApplicablePrice::priority)
					.thenComparing(Comparator.comparing(ApplicablePrice::priceList).reversed());

	private ApplicablePriceSelector() {
	}

	public static Optional<ApplicablePrice> select(List<ApplicablePrice> candidates, LocalDateTime applicationDate) {
		return candidates.stream()
				.filter(candidate -> covers(candidate, applicationDate))
				.max(BY_PRIORITY_THEN_LOWEST_PRICE_LIST);
	}

	private static boolean covers(ApplicablePrice candidate, LocalDateTime applicationDate) {
		return !applicationDate.isBefore(candidate.startDate()) && !applicationDate.isAfter(candidate.endDate());
	}

}
