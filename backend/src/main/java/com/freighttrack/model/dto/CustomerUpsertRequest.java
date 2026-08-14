package com.freighttrack.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerUpsertRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 20) String phone,
        @Size(max = 255) String address,
        @Size(max = 50) String city,
        @Size(max = 50) String state,
        @Size(max = 10) String postalCode,
        Boolean active
) {
}
