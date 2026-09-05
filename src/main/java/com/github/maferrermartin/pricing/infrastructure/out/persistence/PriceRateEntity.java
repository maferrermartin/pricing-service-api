package com.github.maferrermartin.pricing.infrastructure.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRICES", indexes = {
		@Index(name = "idx_prices_lookup", columnList = "BRAND_ID, PRODUCT_ID, START_DATE, END_DATE") })
@SuppressWarnings("unused")
class PriceRateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "BRAND_ID", nullable = false)
	private Long brandId;

	@Column(name = "START_DATE", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "END_DATE", nullable = false)
	private LocalDateTime endDate;

	@Column(name = "PRICE_LIST", nullable = false)
	private Long priceList;

	@Column(name = "PRODUCT_ID", nullable = false)
	private Long productId;

	@Column(name = "PRIORITY", nullable = false)
	private Integer priority;

	@Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "CURR", nullable = false, length = 3)
	private String currency;

	protected PriceRateEntity() {
	}

	PriceRateEntity(Long brandId, LocalDateTime startDate, LocalDateTime endDate, Long priceList,
			Long productId, Integer priority, BigDecimal price, String currency) {
		this.brandId = brandId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.priceList = priceList;
		this.productId = productId;
		this.priority = priority;
		this.price = price;
		this.currency = currency;
	}

	Long getId() {
		return id;
	}

	Long getBrandId() {
		return brandId;
	}

	LocalDateTime getStartDate() {
		return startDate;
	}

	LocalDateTime getEndDate() {
		return endDate;
	}

	Long getPriceList() {
		return priceList;
	}

	Long getProductId() {
		return productId;
	}

	Integer getPriority() {
		return priority;
	}

	BigDecimal getPrice() {
		return price;
	}

	String getCurrency() {
		return currency;
	}

}
