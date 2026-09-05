package com.github.maferrermartin.pricing.infrastructure.in.web;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;

import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

	@Bean
	OpenAPI pricingOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Pricing Service API")
						.description("openapi.info.description")
						.version("v1"));
	}

	@Bean
	OpenApiLocaleCustomizer openApiLocaleCustomizer(MessageSource messageSource) {
		return (openApi, locale) -> {
			if (openApi.getInfo() != null) {
				resolve(messageSource, locale, openApi.getInfo()::getDescription, openApi.getInfo()::setDescription);
			}
			if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
				for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
					translateProperties(schema, messageSource, locale);
				}
			}
		};
	}

	private void translateProperties(Schema<?> schema, MessageSource messageSource, Locale locale) {
		var properties = schema.getProperties();
		if (properties == null) {
			return;
		}
		for (Schema<?> property : properties.values()) {
			resolve(messageSource, locale, property::getDescription, property::setDescription);
		}
	}

	private void resolve(MessageSource messageSource, Locale locale, Supplier<String> getter, Consumer<String> setter) {
		String code = getter.get();
		if (code == null) {
			return;
		}
		try {
			setter.accept(messageSource.getMessage(code, null, locale));
		} catch (NoSuchMessageException ignored) {}
	}

}
