package com.github.maferrermartin.pricing.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

public interface LoadApplicablePricePort {

	List<ApplicablePrice> loadApplicableCandidates(LocalDateTime applicationDate, Long brandId, Long productId);

}
