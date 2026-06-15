package com.medicinereminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MedicineRequest(
        @NotBlank(message = "name is required")
        @Size(max = 50, message = "name must be <= 50 characters")
        String name,

        @Size(max = 200, message = "memo must be <= 200 characters")
        String memo,

        @NotNull(message = "schedules is required")
        @Size(min = 1, max = 6, message = "schedules must be between 1 and 6 items")
        List<@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "schedule must be HH:mm") String> schedules,

        Boolean active
) {
}
