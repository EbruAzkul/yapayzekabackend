package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.model.Doctor;
import com.example.yapayzekabackend.model.DoctorSchedule;
import com.example.yapayzekabackend.service.DoctorScheduleService;
import com.example.yapayzekabackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody DoctorSchedule schedule) {
        try {
            return ResponseEntity.ok(scheduleService.createSchedule(schedule));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getDoctorSchedules(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.findByDoctorId(doctorId));
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {
        try {
            Optional<Doctor> doctor = doctorService.findById(doctorId);
            if (doctor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LocalDate requestedDate = LocalDate.parse(date);
            List<LocalTime> availableSlots = scheduleService.getAvailableTimeSlots(doctorId, requestedDate);

            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.ok(Map.of("message", "Çalışma saati silindi"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}