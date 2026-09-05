package com.github.maferrermartin.pricing.application.port.in;

import java.time.LocalDateTime;
import java.util.Optional;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

public interface FindApplicablePriceQuery {

	Optional<ApplicablePrice> find(LocalDateTime applicationDate, Long brandId, Long productId);

}
