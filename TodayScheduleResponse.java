package com.medicinereminder.dto;

public record TodayScheduleResponse(
        Long medicineId,
        String medicineName,
        String memo,
        String time
) {
}
