package com.freighttrack.model.dto;

import java.time.LocalDateTime;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String city,
        String state,
        String postalCode,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
