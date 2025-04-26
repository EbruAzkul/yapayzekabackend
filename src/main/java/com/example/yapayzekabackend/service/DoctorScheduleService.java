package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.Appointment;
import com.example.yapayzekabackend.model.DoctorSchedule;
import com.example.yapayzekabackend.repository.AppointmentRepository;
import com.example.yapayzekabackend.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    // Sabit değerler
    private static final int APPOINTMENT_DURATION_MINUTES = 30;

    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        // Başlangıç saati bitiş saatinden önce olmalı
        if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new RuntimeException("Başlangıç saati bitiş saatinden sonra olamaz");
        }

        return scheduleRepository.save(schedule);
    }

    public List<DoctorSchedule> findByDoctorId(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    public Optional<DoctorSchedule> findById(Long id) {
        return scheduleRepository.findById(id);
    }

    public List<LocalTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        // 1. O gün için doktorun çalışma saatlerini bul
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        if (schedules.isEmpty()) {
            return List.of(); // Doktorun o gün çalışma saati yok
        }

        // 2. Olası tüm randevu saatlerini hesapla
        List<LocalTime> allPossibleSlots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            if (!schedule.isAvailable()) {
                continue; // Eğer çalışma saati aktif değilse atla
            }

            LocalTime current = schedule.getStartTime();
            while (current.plusMinutes(APPOINTMENT_DURATION_MINUTES).isBefore(schedule.getEndTime())
                    || current.plusMinutes(APPOINTMENT_DURATION_MINUTES).equals(schedule.getEndTime())) {
                allPossibleSlots.add(current);
                current = current.plusMinutes(APPOINTMENT_DURATION_MINUTES);
            }
        }

        // 3. Alınmış randevuları bul ve çıkar
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateBetween(
                doctorId, startOfDay, endOfDay);

        List<LocalTime> bookedTimes = existingAppointments.stream()
                .map(appointment -> appointment.getAppointmentDate().toLocalTime())
                .collect(Collectors.toList());

        // 4. Müsait saatleri döndür
        return allPossibleSlots.stream()
                .filter(time -> !bookedTimes.contains(time))
                .collect(Collectors.toList());
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Çalışma saati bulunamadı");
        }
        scheduleRepository.deleteById(id);
    }
}