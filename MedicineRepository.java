package com.medicinereminder.repository;

import com.medicinereminder.entity.Medicine;
import com.medicinereminder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByUserOrderByCreatedAtDesc(User user);

    Optional<Medicine> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT m FROM Medicine m
            LEFT JOIN FETCH m.schedules
            WHERE m.user = :user
            ORDER BY m.createdAt DESC
            """)
    List<Medicine> findByUserWithSchedules(@Param("user") User user);

    @Query("""
            SELECT m FROM Medicine m
            LEFT JOIN FETCH m.schedules
            WHERE m.id = :id AND m.user = :user
            """)
    Optional<Medicine> findByIdAndUserWithSchedules(@Param("id") Long id, @Param("user") User user);

    @Query("""
            SELECT m FROM Medicine m
            LEFT JOIN FETCH m.schedules
            WHERE m.user = :user AND m.active = true
            ORDER BY m.name ASC
            """)
    List<Medicine> findActiveByUserWithSchedules(@Param("user") User user);
}
