package com.royalpearl.hotel.auth.controller;

import com.royalpearl.hotel.auth.dto.AuthResponse;
import com.royalpearl.hotel.auth.dto.LoginRequest;
import com.royalpearl.hotel.auth.dto.RefreshRequest;
import com.royalpearl.hotel.auth.dto.RegisterRequest;
import com.royalpearl.hotel.auth.service.AuthService;
import com.royalpearl.hotel.common.ApiResponse;
import com.royalpearl.hotel.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new token pair")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke all refresh tokens for the current user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityUtil.currentUserId().ifPresent(authService::logout);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
