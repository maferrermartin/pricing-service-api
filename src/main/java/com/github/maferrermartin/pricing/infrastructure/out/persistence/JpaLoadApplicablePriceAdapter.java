package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import java.util.Currency;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.github.maferrermartin.pricing.application.port.out.LoadApplicablePricePort;
import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

@Component
class JpaLoadApplicablePriceAdapter implements LoadApplicablePricePort {

	private final PriceRateJpaRepository repository;

	JpaLoadApplicablePriceAdapter(PriceRateJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Cacheable("applicablePriceCandidates")
	public List<ApplicablePrice> loadApplicableCandidates(Long brandId, Long productId) {
		return repository
				.findByBrandIdAndProductId(brandId, productId)
				.stream()
				.map(this::toDomain)
				.toList();
	}

	private ApplicablePrice toDomain(PriceRateEntity entity) {
		return new ApplicablePrice(
				entity.getProductId(),
				entity.getBrandId(),
				entity.getPriceList(),
				entity.getPriority(),
				entity.getStartDate(),
				entity.getEndDate(),
				entity.getPrice(),
				Currency.getInstance(entity.getCurrency()));
	}

}
