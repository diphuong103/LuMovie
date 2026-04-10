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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {

        JwtResponse jwtResponse = authService.login(req);

        // Ghi token vào HttpOnly cookie → an toàn hơn lưu localStorage
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtResponse.getAccessToken())
                .httpOnly(true)          // JS không đọc được → chống XSS
                .secure(false)           // true khi dùng HTTPS production
                .path("/")               // gửi lên mọi request
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok(jwtResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký")
    public ResponseEntity<ApiResponse<JwtResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {

        JwtResponse jwtResponse = authService.register(req);

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtResponse.getAccessToken())
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofDays(1)).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("Registered successfully", jwtResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Xóa cookie accessToken
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        authService.logout();

        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới token")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(refreshToken)));
    }
}
