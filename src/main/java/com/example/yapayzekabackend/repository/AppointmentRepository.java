package com.example.yapayzekabackend.repository;

import com.example.yapayzekabackend.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByUserIdAndAppointmentDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByUserIdAndStatusNot(Long userId, String status);
}