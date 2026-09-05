package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.maferrermartin.pricing.application.port.in.FindApplicablePriceQuery;
import com.github.maferrermartin.pricing.domain.model.ApplicablePrice;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

	private static final String ENDPOINT = "/api/v1/prices";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FindApplicablePriceQuery findApplicablePriceQuery;

	@Test
	void returns200WithTheApplicablePriceWhenTheUseCaseFindsOne() throws Exception {
		var start = LocalDateTime.of(2020, 6, 14, 0, 0);
		var end = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
		var applicablePrice = new ApplicablePrice(
				35455L, 1L, 1L, start, end, new BigDecimal("35.50"), Currency.getInstance("EUR"));
		when(findApplicablePriceQuery.find(any(), eq(1L), eq(35455L))).thenReturn(Optional.of(applicablePrice));

		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T10:00:00")
				.param("brandId", "1")
				.param("productId", "35455"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productId").value(35455))
				.andExpect(jsonPath("$.brandId").value(1))
				.andExpect(jsonPath("$.priceList").value(1))
				.andExpect(jsonPath("$.price").value(35.50))
				.andExpect(jsonPath("$.currency").value("EUR"));
	}

	@Test
	void returns404WhenTheUseCaseFindsNoApplicablePrice() throws Exception {
		when(findApplicablePriceQuery.find(any(), eq(1L), eq(35455L))).thenReturn(Optional.empty());

		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-13T10:00:00")
				.param("brandId", "1")
				.param("productId", "35455"))
				.andExpect(status().isNotFound());
	}

}
