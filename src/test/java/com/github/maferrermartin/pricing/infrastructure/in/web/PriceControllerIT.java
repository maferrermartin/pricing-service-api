package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIT {

	private static final String ENDPOINT = "/api/v1/prices";
	private static final String BRAND_ID = "1";
	private static final String PRODUCT_ID = "35455";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void test1_10hDia14_tarifaBase() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T10:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.productId").value(35455))
				.andExpect(jsonPath("$.brandId").value(1))
				.andExpect(jsonPath("$.priceList").value(1))
				.andExpect(jsonPath("$.price").value(35.50))
				.andExpect(jsonPath("$.currency").value("EUR"));
	}

	@Test
	void test2_16hDia14_tarifaSolapadaDeMayorPrioridad() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T16:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priceList").value(2))
				.andExpect(jsonPath("$.price").value(25.45));
	}

	@Test
	void test3_21hDia14_vuelveALaTarifaBaseTrasElSolape() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T21:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priceList").value(1))
				.andExpect(jsonPath("$.price").value(35.50));
	}

	@Test
	void test4_10hDia15_tarifaSolapadaDelDiaSiguiente() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-15T10:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priceList").value(3))
				.andExpect(jsonPath("$.price").value(30.50));
	}

	@Test
	void test5_21hDia16_ultimaTarifaVigente() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-16T21:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priceList").value(4))
				.andExpect(jsonPath("$.price").value(38.95));
	}

	@Test
	void devuelve404CuandoNingunaTarifaCubreLaFecha() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-13T10:00:00")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isNotFound());
	}

	@Test
	void devuelve400CuandoFaltaUnParametroObligatorio() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T10:00:00")
				.param("brandId", BRAND_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("productId")));
	}

	@Test
	void devuelve400ConElFormatoDeFechaEsperadoCuandoLaFechaEsInvalida() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "no-es-una-fecha")
				.param("brandId", BRAND_ID)
				.param("productId", PRODUCT_ID))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("yyyy-MM-ddTHH:mm:ss")));
	}

	@Test
	void devuelve400CuandoElIdDeCadenaNoEsPositivo() throws Exception {
		mockMvc.perform(get(ENDPOINT)
				.param("applicationDate", "2020-06-14T10:00:00")
				.param("brandId", "-1")
				.param("productId", PRODUCT_ID))
				.andExpect(status().isBadRequest());
	}

}
