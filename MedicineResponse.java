package com.medicinereminder.dto;

import java.time.Instant;
import java.util.List;

public record MedicineResponse(
        Long id,
        String name,
        String memo,
        boolean active,
        List<String> schedules,
        Instant createdAt,
        Instant updatedAt
) {
}
