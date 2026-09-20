package com.github.maferrermartin.pricing.application.port.out;

import java.util.List;

import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

public interface LoadApplicablePricePort {

	List<ApplicablePrice> loadApplicableCandidates(Long brandId, Long productId);

}
