package com.example.yapayzekabackend.service;

import com.example.yapayzekabackend.model.User;
import com.example.yapayzekabackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Benzersiz test kullanıcısı oluştur
    public User createUniqueTestUser() {
        String randomEmail = "test_" + UUID.randomUUID() + "@example.com";
        User user = new User();
        user.setName("Test User");
        user.setEmail(randomEmail); // Artık her seferinde unique email
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPublicId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId);
    }

    public User save(User user) {
        if (user.getPublicId() == null) {
            user.setPublicId(UUID.randomUUID().toString()); // Otomatik ID üret
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ❌ publicId ile kullanıcı sil
    public void deleteByPublicId(String publicId) {
        userRepository.findByPublicId(publicId).ifPresent(userRepository::delete);
    }

    // ✏️ Kullanıcıyı güncelle
    public User updateUser(String publicId, User userDetails) {
        Optional<User> optionalUser = userRepository.findByPublicId(publicId);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("Kullanıcı bulunamadı: " + publicId);
        }
    }
}
