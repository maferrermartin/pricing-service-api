package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * It correlates each request with an ID: it respects it if the client already sends it in
 * {@value #REQUEST_ID_HEADER}, otherwise it generates a new one. It returns it in the response and
 * it puts it into the MDC so that it appears in every line of the JSON log for that request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestIdFilter extends OncePerRequestFilter {

	static final String REQUEST_ID_HEADER = "X-Request-Id";
	static final String MDC_KEY = "requestId";

	private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank()) {
			requestId = UUID.randomUUID().toString();
		}

		response.setHeader(REQUEST_ID_HEADER, requestId);
		MDC.put(MDC_KEY, requestId);
		try {
			log.info("{} {}", request.getMethod(), request.getRequestURI());
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

}
