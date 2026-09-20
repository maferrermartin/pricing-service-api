package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.github.maferrermartin.pricing.application.port.out.LoadApplicablePricePort;

@SpringBootTest
class JpaLoadApplicablePriceAdapterCachingIT {

	private static final Long BRAND_ID = 1L;
	private static final Long PRODUCT_ID = 35455L;

	@MockitoSpyBean
	private PriceRateJpaRepository repository;

	@Autowired
	private LoadApplicablePricePort loadApplicablePricePort;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void clearCacheBetweenTests() {
		cacheManager.getCache("applicablePriceCandidates").clear();
	}

	@Test
	void doesNotQueryTheRepositoryAgainForTheSameBrandAndProduct() {
		loadApplicablePricePort.loadApplicableCandidates(BRAND_ID, PRODUCT_ID);
		loadApplicablePricePort.loadApplicableCandidates(BRAND_ID, PRODUCT_ID);

		verify(repository, times(1)).findByBrandIdAndProductId(BRAND_ID, PRODUCT_ID);
	}

	@Test
	void queriesTheRepositoryAgainForADifferentBrandOrProduct() {
		loadApplicablePricePort.loadApplicableCandidates(BRAND_ID, PRODUCT_ID);
		loadApplicablePricePort.loadApplicableCandidates(2L, 999L);

		verify(repository, times(1)).findByBrandIdAndProductId(BRAND_ID, PRODUCT_ID);
		verify(repository, times(1)).findByBrandIdAndProductId(2L, 999L);
	}

}
