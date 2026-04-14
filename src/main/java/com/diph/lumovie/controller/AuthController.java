package com.diph.lumovie.controller;

import com.diph.lumovie.dto.request.*;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin user hiện tại")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities()
        )));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {

        JwtResponse jwtResponse = authService.login(req);

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken",
                jwtResponse.getAccessToken(), Duration.ofDays(1)).toString());

        return ResponseEntity.ok(ApiResponse.ok(jwtResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký")
    public ResponseEntity<ApiResponse<JwtResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {

        JwtResponse jwtResponse = authService.register(req);

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken",
                jwtResponse.getAccessToken(), Duration.ofDays(1)).toString());

        return ResponseEntity.ok(ApiResponse.ok("Registered successfully", jwtResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // maxAge(0) → browser xóa cookie ngay lập tức
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken",
                "", Duration.ZERO).toString());

        authService.logout();

        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới token")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(refreshToken)));
    }

    // ── Helper tránh lặp code tạo cookie ──────────────────────────────────
    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}