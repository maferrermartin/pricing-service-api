package com.github.maferrermartin.pricing.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

/**
 * Picks the winner among overlapping applicable prices: highest priority first;
 * on a tie, the lowest price list.
 */
public final class ApplicablePriceSelector {

	private static final Comparator<ApplicablePrice> BY_PRIORITY_THEN_LOWEST_PRICE_LIST =
			Comparator.comparing(ApplicablePrice::priority)
					.thenComparing(Comparator.comparing(ApplicablePrice::priceList).reversed());

	private ApplicablePriceSelector() {
	}

	public static Optional<ApplicablePrice> highestPriority(List<ApplicablePrice> candidates) {
		return candidates.stream().max(BY_PRIORITY_THEN_LOWEST_PRICE_LIST);
	}

}
