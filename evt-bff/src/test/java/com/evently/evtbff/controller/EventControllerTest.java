
package com.evently.evtbff.controller;

import com.evently.evtbff.client.EventOpenClient;
import com.evently.evtbff.dto.EventHttpResponse;
import com.evently.evtbff.exception.OpenServiceException;
import com.evently.evtbff.service.auth.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventOpenClient eventOpenClient;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getEvent_notFound_returns404Envelope() throws Exception {
        String eventId = "missing-event";

        when(eventOpenClient.getEvent(eventId)).thenThrow(
                new OpenServiceException(
                        404,
                        "Event not found"
                ));

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Event not found"));
    }

    @Test
    void createEvent_invalidRequest_returns400Envelope() throws Exception {
        mockMvc.perform(post("/api/v1/events").requestAttr("role","ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void createEvent_validRequest_returns201() throws Exception {

        String requestBody = """
            {
                "eventName": "Tech Fest",
                "organizerName": "IIIT Bhagalpur",
                "organizerMobile": "9876543210",
                "city": "Bhagalpur",
                "category": "TECH"
            }
            """;

        // Prepare the expected response
        EventHttpResponse response = new EventHttpResponse();
        response.setId("event-123");
        response.setEventName("Tech Fest");
        response.setOrganizerName("IIIT Bhagalpur");
        response.setOrganizerMobile("9876543210");
        response.setCity("Bhagalpur");
        response.setCategory("TECH");
        response.setStatus("PUBLISHED");

        // Configure the existing mock
        when(eventOpenClient.createEvent(any()))
                .thenReturn(response);

        // Send the request and verify the response
        mockMvc.perform(post("/api/v1/events")
                        .requestAttr("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("event-123"))
                .andExpect(jsonPath("$.eventName").value("Tech Fest"))
                .andExpect(jsonPath("$.city").value("Bhagalpur"));
    }

    @Test
    void getEvent_existingId_returns200Envelope() throws Exception {

        String eventId = "event-123";

        EventHttpResponse response = new EventHttpResponse();
        response.setId(eventId);
        response.setEventName("Tech Fest");
        response.setOrganizerName("IIIT Bhagalpur");
        response.setOrganizerMobile("9876543210");
        response.setCity("Bhagalpur");
        response.setCategory("TECH");
        response.setStatus("PUBLISHED");

        when(eventOpenClient.getEvent(eventId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.eventName").value("Tech Fest"))
                .andExpect(jsonPath("$.organizerName").value("IIIT Bhagalpur"))
                .andExpect(jsonPath("$.city").value("Bhagalpur"))
                .andExpect(jsonPath("$.category").value("TECH"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

}
