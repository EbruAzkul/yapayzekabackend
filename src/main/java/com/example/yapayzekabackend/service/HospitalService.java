package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.Hospital;
import com.example.yapayzekabackend.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Hospital> getHospitalsByCity(String city) {
        return hospitalRepository.findByCity(city);
    }

    public List<Hospital> getHospitalsByCityAndDistrict(String city, String district) {
        return hospitalRepository.findByCityAndDistrict(city, district);
    }

    // Yakındaki hastaneleri bulmak için
    public List<Hospital> getNearbyHospitals(Double lat, Double lng, Double radiusKm) {
        // Yaklaşık olarak 1 derece = 111 km
        Double latDiff = radiusKm / 111.0;
        Double lngDiff = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return hospitalRepository.findByLatitudeBetweenAndLongitudeBetween(
                lat - latDiff, lat + latDiff,
                lng - lngDiff, lng + lngDiff
        );
    }

    public Hospital saveHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }
}