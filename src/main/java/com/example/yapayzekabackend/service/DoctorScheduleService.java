package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.Appointment;
import com.example.yapayzekabackend.model.Doctor;
import com.example.yapayzekabackend.model.DoctorSchedule;
import com.example.yapayzekabackend.model.User;
import com.example.yapayzekabackend.repository.AppointmentRepository;
import com.example.yapayzekabackend.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

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
                continue; // Çalışma saati aktif değilse atla
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

    public Map<String, List<String>> getAvailableSlotsForWeek(Long doctorId) {
        Map<String, List<String>> weekSlots = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Bugün ve sonraki 6 gün için müsait saatleri getir
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            List<LocalTime> timeSlots = getAvailableTimeSlots(doctorId, date);

            // Saatleri string formatına çevir
            List<String> formattedSlots = timeSlots.stream()
                    .map(time -> time.toString())
                    .collect(Collectors.toList());

            weekSlots.put(date.toString(), formattedSlots);
        }

        return weekSlots;
    }

    public Appointment bookAppointment(Long doctorId, String userPublicId, LocalDateTime dateTime) {
        // Doktor ve kullanıcıyı bul
        Doctor doctor = doctorService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı"));

        User user = userService.findByPublicId(userPublicId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // O zaman dilimi müsait mi kontrol et
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        List<LocalTime> availableSlots = getAvailableTimeSlots(doctorId, date);
        if (!availableSlots.contains(time)) {
            throw new RuntimeException("Seçilen zaman dilimi müsait değil");
        }

        // Yeni randevu oluştur
        Appointment appointment = Appointment.builder()
                .user(user)
                .doctor(doctor)
                .appointmentDate(dateTime)
                .status("SCHEDULED")
                .build();

        return appointmentRepository.save(appointment);
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Çalışma saati bulunamadı");
        }
        scheduleRepository.deleteById(id);
    }
}