package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PriceRateJpaRepository extends JpaRepository<PriceRateEntity, Long> {

	@Query("""
			SELECT p FROM PriceRateEntity p
			WHERE p.brandId = :brandId
			  AND p.productId = :productId
			  AND p.startDate <= :applicationDate
			  AND p.endDate >= :applicationDate
			ORDER BY p.priority DESC
			""")
	List<PriceRateEntity> findApplicableOrderedByPriority(
			@Param("brandId") Long brandId,
			@Param("productId") Long productId,
			@Param("applicationDate") LocalDateTime applicationDate,
			Limit limit);

}
