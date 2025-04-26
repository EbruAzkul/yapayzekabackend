package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.model.Appointment;
import com.example.yapayzekabackend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointment));
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> getUserAppointments(@PathVariable Long userId) {
        return ResponseEntity.ok(appointmentService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.findById(id);
        if (appointment.isPresent()) {
            return ResponseEntity.ok(appointment.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Randevu bulunamadı"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Appointment updatedAppointment = appointmentService.updateStatus(id, status);
            return ResponseEntity.ok(updatedAppointment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(Map.of("message", "Randevu iptal edildi"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}