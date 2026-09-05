package com.github.maferrermartin.pricing.application;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.github.maferrermartin.pricing.application.port.in.FindApplicablePriceQuery;
import com.github.maferrermartin.pricing.application.port.out.LoadApplicablePricePort;
import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

@Service
class PricingApplicationService implements FindApplicablePriceQuery {

	private final LoadApplicablePricePort loadApplicablePricePort;

	PricingApplicationService(LoadApplicablePricePort loadApplicablePricePort) {
		this.loadApplicablePricePort = loadApplicablePricePort;
	}

	@Override
	public Optional<ApplicablePrice> find(LocalDateTime applicationDate, Long brandId, Long productId) {
		return loadApplicablePricePort.loadApplicablePrice(applicationDate, brandId, productId);
	}

}
