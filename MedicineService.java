package com.medicinereminder.service;

import com.medicinereminder.dto.MedicineRequest;
import com.medicinereminder.dto.MedicineResponse;
import com.medicinereminder.dto.TodayScheduleResponse;
import com.medicinereminder.entity.Medicine;
import com.medicinereminder.entity.MedicineSchedule;
import com.medicinereminder.entity.User;
import com.medicinereminder.repository.MedicineRepository;
import com.medicinereminder.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MedicineService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    public MedicineService(MedicineRepository medicineRepository, UserRepository userRepository) {
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> findAll(User user) {
        return medicineRepository.findByUserWithSchedules(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicineResponse findById(User user, Long id) {
        return toResponse(getMedicine(user, id));
    }

    @Transactional(readOnly = true)
    public List<TodayScheduleResponse> findToday(User user) {
        return medicineRepository.findActiveByUserWithSchedules(user).stream()
                .flatMap(medicine -> medicine.getSchedules().stream()
                        .map(schedule -> new TodayScheduleResponse(
                                medicine.getId(),
                                medicine.getName(),
                                medicine.getMemo(),
                                schedule.getTimeOfDay().format(TIME_FORMAT)
                        )))
                .sorted(Comparator.comparing(TodayScheduleResponse::time))
                .toList();
    }

    @Transactional
    public MedicineResponse create(User user, MedicineRequest request) {
        validateSchedules(request.schedules());

        Medicine medicine = new Medicine();
        medicine.setUser(user);
        applyRequest(medicine, request);
        medicineRepository.save(medicine);
        return toResponse(medicine);
    }

    @Transactional
    public MedicineResponse update(User user, Long id, MedicineRequest request) {
        validateSchedules(request.schedules());

        Medicine medicine = getMedicine(user, id);
        applyRequest(medicine, request);
        return toResponse(medicine);
    }

    @Transactional
    public void delete(User user, Long id) {
        Medicine medicine = getMedicine(user, id);
        medicineRepository.delete(medicine);
    }

    private Medicine getMedicine(User user, Long id) {
        return medicineRepository.findByIdAndUserWithSchedules(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));
    }

    private void applyRequest(Medicine medicine, MedicineRequest request) {
        medicine.setName(request.name().trim());
        medicine.setMemo(request.memo() == null ? null : request.memo().trim());
        if (request.active() != null) {
            medicine.setActive(request.active());
        }

        medicine.getSchedules().clear();
        List<LocalTime> times = parseSchedules(request.schedules());
        for (LocalTime time : times) {
            MedicineSchedule schedule = new MedicineSchedule();
            schedule.setMedicine(medicine);
            schedule.setTimeOfDay(time);
            medicine.getSchedules().add(schedule);
        }
    }

    private void validateSchedules(List<String> schedules) {
        Set<String> unique = new HashSet<>(schedules);
        if (unique.size() != schedules.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "duplicate time in schedules");
        }
    }

    private List<LocalTime> parseSchedules(List<String> schedules) {
        List<LocalTime> times = new ArrayList<>();
        for (String schedule : schedules) {
            times.add(LocalTime.parse(schedule, TIME_FORMAT));
        }
        times.sort(Comparator.naturalOrder());
        return times;
    }

    private MedicineResponse toResponse(Medicine medicine) {
        List<String> schedules = medicine.getSchedules().stream()
                .sorted(Comparator.comparing(MedicineSchedule::getTimeOfDay))
                .map(schedule -> schedule.getTimeOfDay().format(TIME_FORMAT))
                .toList();

        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getMemo(),
                medicine.isActive(),
                schedules,
                medicine.getCreatedAt(),
                medicine.getUpdatedAt()
        );
    }

    public User getDefaultUser() {
        return userRepository.findByLineUserId("dev-user")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default user not initialized"
                ));
    }
}
