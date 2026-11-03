
package com.evently.evtbff.controller;

import com.evently.evtbff.client.EventOpenClient;
import com.evently.evtbff.service.auth.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.evently.evtbff.model.auth.User;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@WebMvcTest(EventController.class)
@AutoConfigureMockMvc
@Import({
        com.evently.evtbff.config.SecurityConfig.class,
        JwtService.class
})
@TestPropertySource(properties = {
        "jwt.secret=ThisIsAValidTestSecretKeyForHmacSha256Signing123456"
})
class EventSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventOpenClient eventOpenClient;

    @Autowired
    private JwtService jwtService;

    @Test
    void getEvent_withoutToken_returns401() throws Exception {

        mockMvc.perform(get("/api/v1/events/event-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void createEvent_viewerRole_returns403() throws Exception {

        User viewer = new User(
                "user-2",
                "8765432109",
                "VIEWER"
        );

        String token = jwtService.generateAccessToken(viewer);

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "eventName": "Tech Fest",
                            "organizerName": "Test User",
                            "organizerMobile": "9876543210",
                            "city": "Bhagalpur",
                            "category": "TECH"
                        }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
