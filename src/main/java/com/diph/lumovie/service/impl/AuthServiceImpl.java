package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.request.*;
import com.diph.lumovie.dto.response.JwtResponse;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.exception.*;
import com.diph.lumovie.mapper.UserMapper;
import com.diph.lumovie.repository.UserRepository;
import com.diph.lumovie.security.JwtUtil;
import com.diph.lumovie.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override @Transactional
    public JwtResponse login(LoginRequest request) {
        // 1. Tìm user qua username hoặc email
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        // 2. Xác thực password qua Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Mật khẩu không chính xác!");
        }

        // 3. Cập nhật lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Tạo token — LUÔN dùng username làm subject
        String accessToken = jwtUtil.generateToken(user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override @Transactional
    public JwtResponse register(RegisterRequest request) {
        // 1. Kiểm tra trùng email VÀ username
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateEmailException(request.getEmail());
        if (userRepository.existsByUsername(request.getUsername()))
            throw new DuplicateResourceException("Username đã tồn tại: " + request.getUsername());

        // 2. Build và lưu user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        // 3. Tạo fullName mặc định nếu bỏ trống
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName("User_" + user.getId());
            userRepository.save(user);
        }

        // 4. Token dùng username — nhất quán với login
        String accessToken = jwtUtil.generateToken(user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) {
        throw new UnsupportedOperationException("Refresh token chưa được triển khai");
    }

    @Override
    public void logout() {
        // Cookie đã được xóa ở AuthController
        // Có thể thêm logic revoke refresh token ở đây sau này
    }
}
