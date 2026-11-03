
package com.evently.evtbff;

import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "event.open-service.url=http://localhost:8081",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379"
        }
)
@AutoConfigureTestRestTemplate
class EventlyEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String MOBILE = "9876543210";

    private static final String NOTIFICATION_URL =
            "http://localhost:8083/v1/dashboard/";

    @Test
    void shouldCreateEventAndUpdateCityDashboard() {

        String baseUrl = "http://localhost:" + port;

        // STEP 1: Generate OTP

        ResponseEntity<JsonNode> otpResponse =
                restTemplate.postForEntity(
                        baseUrl + "/api/v1/auth/generate-otp",
                        Map.of("mobile", MOBILE),
                        JsonNode.class
                );

        assertThat(otpResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // STEP 2: Read OTP from Redis

        String otp = redisTemplate.opsForValue()
                .get("otp:" + MOBILE);

        assertThat(otp).isNotBlank();

        // STEP 3: Verify OTP and login

        ResponseEntity<JsonNode> loginResponse =
                restTemplate.postForEntity(
                        baseUrl + "/api/v1/auth/verify-otp",
                        Map.of(
                                "mobile", MOBILE,
                                "otp", otp
                        ),
                        JsonNode.class
                );

        assertThat(loginResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        String accessToken = loginResponse.getBody()
                .path("data")
                .path("accessToken")
                .asText();

        assertThat(accessToken).isNotBlank();

        // STEP 4: Prepare a unique city for this test

        String city = "E2E-" + UUID.randomUUID()
                .toString().substring(0, 8);

        // STEP 5: Create event

        String organizerMobile = "9726227462";

        Map<String, String> eventRequest = Map.of(
                "eventName", "Evently Integration Test",
                "organizerName", "Test Organizer",
                "organizerMobile", organizerMobile,
                "city", city,
                "category", "WORKSHOP"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(eventRequest, headers);

        ResponseEntity<JsonNode> eventResponse =
                restTemplate.postForEntity(
                        baseUrl + "/api/v1/events",
                        request,
                        JsonNode.class
                );

        // DEBUG: Print the actual response

        System.out.println("\n========== CREATE EVENT RESPONSE ==========");
        System.out.println("Status: " + eventResponse.getStatusCode());
        System.out.println("Body: " + eventResponse.getBody());
        System.out.println("============================================\n");

        assertThat(eventResponse.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        String eventId = eventResponse.getBody()
                .path("id")
                .asText();

        assertThat(eventId).isNotBlank();

        // STEP 6: Wait for Kafka processing and MongoDB update

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {

                    ResponseEntity<JsonNode> dashboardResponse =
                            restTemplate.getForEntity(
                                    NOTIFICATION_URL + city,
                                    JsonNode.class
                            );

                    assertThat(dashboardResponse.getStatusCode())
                            .isEqualTo(HttpStatus.OK);

                    JsonNode dashboard = dashboardResponse.getBody();

                    assertThat(dashboard.path("totalEvents").asLong())
                            .isEqualTo(1L);

                    assertThat(dashboard.path("publishedEvents").asLong())
                            .isEqualTo(0L);

                    assertThat(
                            dashboard.path("eventsByCategory")
                                    .path("WORKSHOP").asLong()
                    ).isEqualTo(1L);

                    assertThat(
                            dashboard.path("eventStatuses")
                                    .path(eventId).asText()
                    ).isEqualTo("DRAFT");
                });
    }
}
