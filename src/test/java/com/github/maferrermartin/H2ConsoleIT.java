package com.github.maferrermartin;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class H2ConsoleIT {

	@Test
	void theH2ConsoleServletIsReachable() throws Exception {
		var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
		var request = HttpRequest.newBuilder(URI.create("http://localhost:8080/h2-console")).GET().build();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
				contentType -> assertThat(contentType).contains("text/html"));
	}

}
