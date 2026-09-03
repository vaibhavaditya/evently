package com.evently.evtcoreservice.dto;

import com.evently.evtcoreservice.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private EventStatus status;
}