package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import org.springframework.data.domain.Limit;
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
	public Optional<ApplicablePrice> loadApplicablePrice(LocalDateTime applicationDate, Long brandId, Long productId) {
		return repository
				.findApplicableOrderedByPriority(brandId, productId, applicationDate, Limit.of(1))
				.stream()
				.findFirst()
				.map(this::toDomain);
	}

	private ApplicablePrice toDomain(PriceRateEntity entity) {
		return new ApplicablePrice(
				entity.getProductId(),
				entity.getBrandId(),
				entity.getPriceList(),
				entity.getStartDate(),
				entity.getEndDate(),
				entity.getPrice(),
				Currency.getInstance(entity.getCurrency()));
	}

}
