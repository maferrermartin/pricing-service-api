package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.data.domain.Limit;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

@ExtendWith(MockitoExtension.class)
class JpaLoadApplicablePriceAdapterTest {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;
	private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0);

	@Mock
	private PriceRateJpaRepository repository;

	private JpaLoadApplicablePriceAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaLoadApplicablePriceAdapter(repository);
	}

	@Test
	void mapsTheEntityReturnedByTheRepositoryToTheDomainValueObject() {
		var start = LocalDateTime.of(2020, 6, 14, 15, 0);
		var end = LocalDateTime.of(2020, 6, 14, 18, 30);
		var entity = new PriceRateEntity(BRAND_ID, start, end, 2L, PRODUCT_ID, 1, new BigDecimal("25.45"), "EUR");
		when(repository.findApplicableOrderedByPriority(BRAND_ID, PRODUCT_ID, APPLICATION_DATE, Limit.of(1)))
				.thenReturn(List.of(entity));

		var result = adapter.loadApplicablePrice(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).contains(new ApplicablePrice(
				PRODUCT_ID, BRAND_ID, 2L, start, end, new BigDecimal("25.45"), Currency.getInstance("EUR")));
	}

	@Test
	void returnsEmptyWhenTheRepositoryFindsNoCandidate() {
		when(repository.findApplicableOrderedByPriority(BRAND_ID, PRODUCT_ID, APPLICATION_DATE, Limit.of(1)))
				.thenReturn(List.of());

		var result = adapter.loadApplicablePrice(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).isEmpty();
	}

}
