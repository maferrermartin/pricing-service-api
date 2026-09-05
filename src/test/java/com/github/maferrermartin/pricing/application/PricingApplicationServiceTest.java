package com.github.maferrermartin.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

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
	void delegatesToTheOutputPortWithTheSameArgumentsAndReturnsItsResult() {
		var applicablePrice = new ApplicablePrice(PRODUCT_ID, BRAND_ID, 1L,
				APPLICATION_DATE, APPLICATION_DATE.plusDays(1), new BigDecimal("35.50"), Currency.getInstance("EUR"));
		when(loadApplicablePricePort.loadApplicablePrice(APPLICATION_DATE, BRAND_ID, PRODUCT_ID))
				.thenReturn(Optional.of(applicablePrice));

		var result = service.find(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).contains(applicablePrice);
		verify(loadApplicablePricePort).loadApplicablePrice(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);
	}

	@Test
	void returnsEmptyWhenTheOutputPortFindsNothing() {
		when(loadApplicablePricePort.loadApplicablePrice(APPLICATION_DATE, BRAND_ID, PRODUCT_ID))
				.thenReturn(Optional.empty());

		var result = service.find(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).isEmpty();
	}

}
