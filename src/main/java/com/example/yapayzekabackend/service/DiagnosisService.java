// ✅ DiagnosisService.java
package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.Diagnosis;
import com.example.yapayzekabackend.model.DiagnosisResult;
import com.example.yapayzekabackend.model.User;
import com.example.yapayzekabackend.repository.DiagnosisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DiagnosisService {

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private ModelService modelService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    public Diagnosis createTestDiagnosis(MultipartFile imageFile, String notes) throws IOException {
        User testUser = userService.createUniqueTestUser();

        String fileName = fileStorageService.storeFile(imageFile, testUser.getPublicId());
        File savedFile = fileStorageService.getFile(fileName);

        DiagnosisResult result = modelService.predict(savedFile);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setUser(testUser);
        diagnosis.setImagePath(fileName);
        diagnosis.setPredictedClass(result.getPredictedClass());
        diagnosis.setConfidence(result.getConfidence());
        diagnosis.setAllProbabilities(result.getAllProbabilities());
        diagnosis.setNotes(notes);

        return diagnosisRepository.save(diagnosis);
    }

    public List<Diagnosis> getAllDiagnoses() {
        return diagnosisRepository.findAll();
    }

    public Optional<Diagnosis> getDiagnosisById(Long id) {
        return diagnosisRepository.findById(id);
    }
}