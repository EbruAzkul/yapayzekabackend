package com.example.yapayzekabackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialty;
    private String hospital;
    private String contactNumber;

    @ElementCollection
    private List<String> availableDays; // ["MONDAY", "WEDNESDAY"]
}