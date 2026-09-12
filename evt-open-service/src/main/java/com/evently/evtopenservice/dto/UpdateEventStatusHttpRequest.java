package com.evently.evtopenservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventStatusHttpRequest {

    @NotBlank(message = "Status is required")
    private String status;
}