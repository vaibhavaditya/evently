package com.evently.evtbff.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventStatusHttpRequest {
    @NotBlank(message = "status is required")
    private String status;
}