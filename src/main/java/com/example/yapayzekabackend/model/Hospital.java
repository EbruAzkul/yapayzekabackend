package com.example.yapayzekabackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "hospitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String district;
    private String address;
    private String phone;

    // Opsiyonel olarak coğrafi konumu da ekleyebiliriz
    private Double latitude;
    private Double longitude;

    // Hastanede çalışan doktorları almak için
    @OneToMany(mappedBy = "hospital")
    private List<Doctor> doctors;
}