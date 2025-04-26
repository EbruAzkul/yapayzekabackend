package com.example.yapayzekabackend.controller;


import com.example.yapayzekabackend.model.Doctor;
import com.example.yapayzekabackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createDoctor(doctor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors(
            @RequestParam(required = false) String specialty) {
        if (specialty != null) {
            return ResponseEntity.ok(doctorService.findBySpecialty(specialty));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }
}