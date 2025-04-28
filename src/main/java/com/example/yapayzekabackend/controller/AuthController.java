package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.dto.LoginRequest;
import com.example.yapayzekabackend.dto.LoginResponse;
import com.example.yapayzekabackend.dto.RegisterRequest;
import com.example.yapayzekabackend.model.User;
import com.example.yapayzekabackend.security.JwtTokenUtil;
import com.example.yapayzekabackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı kaydı, giriş ve token işlemleri")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Kullanıcı Kaydı", description = "Yeni bir kullanıcı oluşturur ve kayıt bilgilerini döndürür")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Email kontrolü
        if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu email adresi zaten kullanılıyor");
        }

        // User nesnesini oluştur
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // PublicId kontrolü
        if (registerRequest.getPublicId() == null || registerRequest.getPublicId().trim().isEmpty()) {
            user.setPublicId(UUID.randomUUID().toString());
        } else {
            user.setPublicId(registerRequest.getPublicId());
        }

        User savedUser = userService.save(user);

        // Kullanıcı oluşturulduktan sonra otomatik olarak token üret
        String token = jwtTokenUtil.generateToken(savedUser.getEmail());

        LoginResponse response = new LoginResponse(
                token,
                savedUser.getPublicId(),
                savedUser.getName(),
                savedUser.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi", description = "Email ve şifre ile kullanıcı girişi yapar ve token döndürür")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Önce kullanıcıyı bul
            Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Kullanici bulunamadi");
            }

            User user = userOpt.get();

            // Şifreyi kontrol et
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Gecersiz email veya sifre");
            }

            // Authentication işlemini gerçekleştir
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Token oluştur
            String token = jwtTokenUtil.generateToken(user.getEmail());

            LoginResponse response = new LoginResponse(
                    token,
                    user.getPublicId(),
                    user.getName(),
                    user.getEmail()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Gecersiz email veya sifre");
        }
    }
}