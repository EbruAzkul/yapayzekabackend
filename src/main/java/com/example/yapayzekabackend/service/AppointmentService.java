package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.Appointment;
import com.example.yapayzekabackend.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }


    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }


    public List<Appointment> findByUserId(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }


    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }


    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadi"));

        // Durum kontrolu yap
        if (!isValidStatus(status)) {
            throw new RuntimeException("Gecersiz randevu durumu: " + status);
        }

        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadi"));

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    private boolean isValidStatus(String status) {
        return status != null && (
                status.equals("SCHEDULED") ||
                        status.equals("COMPLETED") ||
                        status.equals("CANCELLED"));
    }
}