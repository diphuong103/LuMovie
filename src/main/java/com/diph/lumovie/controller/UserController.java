package com.diph.lumovie.controller;
import com.diph.lumovie.dto.request.UpdateProfileRequest;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUser(u.getUsername())));
    }
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
        @AuthenticationPrincipal UserDetails u, @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(u.getUsername(), req)));
    }
}
