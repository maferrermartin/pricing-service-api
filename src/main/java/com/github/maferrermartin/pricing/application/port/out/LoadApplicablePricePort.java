package com.github.maferrermartin.pricing.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

public interface LoadApplicablePricePort {

	Optional<ApplicablePrice> loadApplicablePrice(LocalDateTime applicationDate, Long brandId, Long productId);

}
