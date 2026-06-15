package com.medicinereminder.controller;

import com.medicinereminder.dto.MedicineRequest;
import com.medicinereminder.dto.MedicineResponse;
import com.medicinereminder.dto.TodayScheduleResponse;
import com.medicinereminder.entity.User;
import com.medicinereminder.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public List<MedicineResponse> list() {
        return medicineService.findAll(currentUser());
    }

    @GetMapping("/today")
    public List<TodayScheduleResponse> today() {
        return medicineService.findToday(currentUser());
    }

    @GetMapping("/{id}")
    public MedicineResponse detail(@PathVariable Long id) {
        return medicineService.findById(currentUser(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse create(@Valid @RequestBody MedicineRequest request) {
        return medicineService.create(currentUser(), request);
    }

    @PutMapping("/{id}")
    public MedicineResponse update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return medicineService.update(currentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        medicineService.delete(currentUser(), id);
    }

    private User currentUser() {
        return medicineService.getDefaultUser();
    }
}
