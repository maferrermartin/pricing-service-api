package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface PriceRateJpaRepository extends JpaRepository<PriceRateEntity, Long> {

	List<PriceRateEntity> findByBrandIdAndProductId(Long brandId, Long productId);

}
