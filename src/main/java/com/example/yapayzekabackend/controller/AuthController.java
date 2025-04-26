package com.example.yapayzekabackend.controller;

import com.example.yapayzekabackend.dto.LoginRequest;
import com.example.yapayzekabackend.dto.LoginResponse;
import com.example.yapayzekabackend.model.User;
import com.example.yapayzekabackend.security.JwtTokenUtil;
import com.example.yapayzekabackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Kimlik Doğrulama", description = "Kullanıcı Giriş ve Kimlik İşlemleri")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi", description = "Email ve şifre ile kullanıcı girişi")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Kullanici bulunamadi");
            }

            User user = userOpt.get();
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