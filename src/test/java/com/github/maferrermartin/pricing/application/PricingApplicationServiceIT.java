package com.github.maferrermartin.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.maferrermartin.pricing.application.port.in.FindApplicablePriceQuery;

@SpringBootTest
class PricingApplicationServiceIT {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;

	@Autowired
	private FindApplicablePriceQuery findApplicablePriceQuery;

	@Test
	void returnsBasePriceWhenNoOtherRateApplies() {
		var result = findApplicablePriceQuery.find(
				LocalDateTime.of(2020, 6, 14, 10, 0), BRAND_ID, PRODUCT_ID);

		assertThat(result).isPresent();
		assertThat(result.get().priceList()).isEqualTo(1L);
		assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("35.50"));
	}

	@Test
	void returnsHighestPriorityRateWhenRangesOverlap() {
		var result = findApplicablePriceQuery.find(
				LocalDateTime.of(2020, 6, 14, 16, 0), BRAND_ID, PRODUCT_ID);

		assertThat(result).isPresent();
		assertThat(result.get().priceList()).isEqualTo(2L);
		assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("25.45"));
	}

	@Test
	void returnsHighestPriorityRateOnLastOverlappingWindow() {
		var result = findApplicablePriceQuery.find(
				LocalDateTime.of(2020, 6, 16, 21, 0), BRAND_ID, PRODUCT_ID);

		assertThat(result).isPresent();
		assertThat(result.get().priceList()).isEqualTo(4L);
		assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("38.95"));
	}

	@Test
	void isEmptyWhenNoRateCoversTheDate() {
		var result = findApplicablePriceQuery.find(
				LocalDateTime.of(2020, 6, 13, 10, 0), BRAND_ID, PRODUCT_ID);

		assertThat(result).isEmpty();
	}

}
