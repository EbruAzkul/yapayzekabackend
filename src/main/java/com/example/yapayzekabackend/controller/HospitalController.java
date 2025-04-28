package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.model.Hospital;
import com.example.yapayzekabackend.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/by-city")
    public ResponseEntity<List<Hospital>> getHospitalsByCity(@RequestParam String city) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCity(city));
    }

    @GetMapping("/by-city-district")
    public ResponseEntity<List<Hospital>> getHospitalsByCityAndDistrict(
            @RequestParam String city,
            @RequestParam String district) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCityAndDistrict(city, district));
    }

    @GetMapping("/by-location")
    public ResponseEntity<List<Hospital>> getNearbyHospitals(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        return ResponseEntity.ok(hospitalService.getNearbyHospitals(latitude, longitude, radiusKm));
    }

    @PostMapping
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
        return ResponseEntity.ok(hospitalService.saveHospital(hospital));
    }
}