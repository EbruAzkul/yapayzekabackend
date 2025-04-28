package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.model.Appointment;
import com.example.yapayzekabackend.model.Doctor;
import com.example.yapayzekabackend.model.DoctorSchedule;
import com.example.yapayzekabackend.service.DoctorScheduleService;
import com.example.yapayzekabackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getDoctorSchedules(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.findByDoctorId(doctorId));
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Optional<Doctor> doctor = doctorService.findById(doctorId);
            if (doctor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<LocalTime> availableSlots = scheduleService.getAvailableTimeSlots(doctorId, date);

            // String formatında saatleri listeye çevirme
            List<String> formattedSlots = availableSlots.stream()
                    .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(formattedSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/available-week")
    public ResponseEntity<?> getAvailableSlotsForWeek(@RequestParam Long doctorId) {
        try {
            Map<String, List<String>> availableSlots = scheduleService.getAvailableSlotsForWeek(doctorId);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/book-appointment")
    public ResponseEntity<?> bookAppointment(
            @RequestParam Long doctorId,
            @RequestParam String userPublicId,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime) {

        try {
            // dateTime string'ini parse et - "2023-05-15" ve "14:30"
            LocalDateTime dateTime = LocalDateTime.parse(
                    appointmentDate + "T" + appointmentTime + ":00"
            );

            Appointment appointment = scheduleService.bookAppointment(doctorId, userPublicId, dateTime);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.ok(Map.of("message", "Çalışma saati silindi"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}