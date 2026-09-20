package com.github.maferrermartin.pricing.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;

class OpenApiConfigTest {

	private final OpenApiConfig config = new OpenApiConfig();

	@Test
	void buildsTheApiInfoWithATitleAndVersion() {
		var openApi = config.pricingOpenApi();

		assertThat(openApi.getInfo().getTitle()).isEqualTo("Pricing Service API");
		assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
		assertThat(openApi.getInfo().getDescription()).isEqualTo("openapi.info.description");
	}

	@Test
	void translatesTheApiDescriptionUsingTheMessageSource() {
		var messageSource = new StaticMessageSource();
		messageSource.addMessage("openapi.info.description", Locale.ENGLISH, "Pricing API");
		var openApi = new OpenAPI().info(new Info().description("openapi.info.description"));

		config.openApiLocaleCustomizer(messageSource).customise(openApi, Locale.ENGLISH);

		assertThat(openApi.getInfo().getDescription()).isEqualTo("Pricing API");
	}

	@Test
	void translatesEverySchemaPropertyDescription() {
		var messageSource = new StaticMessageSource();
		messageSource.addMessage("openapi.schema.price.brand-id", Locale.ENGLISH, "Brand id");
		var brandIdProperty = new Schema<>().description("openapi.schema.price.brand-id");
		var schema = new Schema<>().properties(Map.of("brandId", brandIdProperty));
		var openApi = new OpenAPI().components(new Components().addSchemas("PriceResponse", schema));

		config.openApiLocaleCustomizer(messageSource).customise(openApi, Locale.ENGLISH);

		assertThat(brandIdProperty.getDescription()).isEqualTo("Brand id");
	}

	@Test
	void leavesTheDescriptionAsIsWhenThereIsNoTranslationForTheCode() {
		var messageSource = new StaticMessageSource();
		var openApi = new OpenAPI().info(new Info().description("unknown.code"));

		config.openApiLocaleCustomizer(messageSource).customise(openApi, Locale.ENGLISH);

		assertThat(openApi.getInfo().getDescription()).isEqualTo("unknown.code");
	}

	@Test
	void doesNothingWhenTheOpenApiHasNoInfoOrComponents() {
		var messageSource = new StaticMessageSource();
		var openApi = new OpenAPI();

		config.openApiLocaleCustomizer(messageSource).customise(openApi, Locale.ENGLISH);

		assertThat(openApi.getInfo()).isNull();
		assertThat(openApi.getComponents()).isNull();
	}

	@Test
	void skipsASchemaThatHasNoProperties() {
		var messageSource = new StaticMessageSource();
		var schemaWithoutProperties = new Schema<>().description("openapi.schema.price.brand-id");
		var openApi = new OpenAPI().components(new Components().addSchemas("Empty", schemaWithoutProperties));

		config.openApiLocaleCustomizer(messageSource).customise(openApi, Locale.ENGLISH);

		assertThat(schemaWithoutProperties.getDescription()).isEqualTo("openapi.schema.price.brand-id");
	}

}
