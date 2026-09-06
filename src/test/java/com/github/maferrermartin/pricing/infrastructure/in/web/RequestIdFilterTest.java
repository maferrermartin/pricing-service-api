package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class RequestIdFilterTest {

	private final RequestIdFilter filter = new RequestIdFilter();

	@Test
	void generatesARequestIdWhenTheClientDoesNotSendOne() throws Exception {
		var request = new MockHttpServletRequest("GET", "/api/v1/prices");
		var response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		String generated = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
		assertThat(generated).isNotBlank();
		verify(chain).doFilter(request, response);
		assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
	}

	@Test
	void reusesTheClientSuppliedRequestId() throws Exception {
		var request = new MockHttpServletRequest("GET", "/api/v1/prices");
		request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "client-supplied-id");
		var response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("client-supplied-id");
	}

	@Test
	void putsTheRequestIdInTheMdcWhileTheChainRuns() throws Exception {
		var request = new MockHttpServletRequest("GET", "/api/v1/prices");
		var response = new MockHttpServletResponse();
		var mdcValueDuringChain = new String[1];
		FilterChain chain = (req, res) -> mdcValueDuringChain[0] = MDC.get(RequestIdFilter.MDC_KEY);

		filter.doFilter(request, response, chain);

		assertThat(mdcValueDuringChain[0]).isEqualTo(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
	}

}
