package com.evently.evtcoreservice.dto;

import com.evently.evtcoreservice.enums.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventRequest {

    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    @NotBlank(message = "Organizer mobile is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Organizer mobile must be a valid 10-digit mobile number"
    )
    private String organizerMobile;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Category is required")
    private EventCategory category;
}