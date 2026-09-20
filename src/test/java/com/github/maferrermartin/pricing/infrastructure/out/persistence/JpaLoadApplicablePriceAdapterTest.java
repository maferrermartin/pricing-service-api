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
	void mapsEveryEntityReturnedByTheRepositoryToADomainValueObject() {
		var start = LocalDateTime.of(2020, 6, 14, 15, 0);
		var end = LocalDateTime.of(2020, 6, 14, 18, 30);
		var overlapping = new PriceRateEntity(BRAND_ID, start, end, 2L, PRODUCT_ID, 1, new BigDecimal("25.45"), "EUR");
		var base = new PriceRateEntity(BRAND_ID, start, end, 1L, PRODUCT_ID, 0, new BigDecimal("35.50"), "EUR");
		when(repository.findApplicable(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
				.thenReturn(List.of(overlapping, base));

		var result = adapter.loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).containsExactly(
				new ApplicablePrice(PRODUCT_ID, BRAND_ID, 2L, 1, start, end, new BigDecimal("25.45"), Currency.getInstance("EUR")),
				new ApplicablePrice(PRODUCT_ID, BRAND_ID, 1L, 0, start, end, new BigDecimal("35.50"), Currency.getInstance("EUR")));
	}

	@Test
	void returnsAnEmptyListWhenTheRepositoryFindsNoCandidate() {
		when(repository.findApplicable(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
				.thenReturn(List.of());

		var result = adapter.loadApplicableCandidates(APPLICATION_DATE, BRAND_ID, PRODUCT_ID);

		assertThat(result).isEmpty();
	}

}
