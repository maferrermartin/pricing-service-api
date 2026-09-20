package com.github.maferrermartin.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.maferrermartin.pricing.application.port.out.LoadApplicablePricePort;
import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

@ExtendWith(MockitoExtension.class)
class PricingApplicationServiceTest {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;
	private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

	@Mock
	private LoadApplicablePricePort loadApplicablePricePort;

	private PricingApplicationService service;

	@BeforeEach
	void setUp() {
		service = new PricingApplicationService(loadApplicablePricePort);
	}

	@Test
	void asksThePortForCandidatesWithTheSameArguments() {
		when(loadApplicablePricePort.loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID))
				.thenReturn(List.of());

		service.find(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		verify(loadApplicablePricePort).loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);
	}

	@Test
	void returnsTheHighestPriorityCandidateWhenSeveralOverlap() {
		var basePrice = candidateWith(1L, 0, new BigDecimal("35.50"));
		var higherPriority = candidateWith(2L, 1, new BigDecimal("25.45"));
		when(loadApplicablePricePort.loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID))
				.thenReturn(List.of(basePrice, higherPriority));

		var result = service.find(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).contains(higherPriority);
	}

	@Test
	void returnsEmptyWhenThePortFindsNoCandidate() {
		when(loadApplicablePricePort.loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID))
				.thenReturn(List.of());

		var result = service.find(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).isEmpty();
	}

	private ApplicablePrice candidateWith(Long priceList, int priority, BigDecimal price) {
		return new ApplicablePrice(PRODUCT_ID, BRAND_ID, priceList, priority,
				APPLICATION_DATE, APPLICATION_DATE.plusDays(1), price, Currency.getInstance("EUR"));
	}

}
