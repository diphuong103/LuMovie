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

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override @Transactional
    public JwtResponse login(LoginRequest request) {
        // 1. Chủ động tìm User bằng cả Username HOẶC Email trước
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        try {
            // 2. Xác thực bằng Username chuẩn từ DB (user.getUsername())
            // để đảm bảo Spring Security tìm đúng record trong UserDetailsService
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        // 3. Tạo Token
        String token = jwtUtil.generateToken(user.getUsername());

        return JwtResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override @Transactional
    public JwtResponse register(RegisterRequest request) {
        // 1. Kiểm tra tồn tại
        if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateEmailException(request.getEmail());

        // 2. Build user (tạm thời để fullName là null nếu request không có)
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        // 3. Lưu lần đầu để lấy ID
        user = userRepository.save(user);

        // 4. Nếu fullName null, cộng ID vào để tạo chuỗi duy nhất
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName("User_" + user.getId());
            // Kết quả: User_1, User_2, User_100...
        }

        return JwtResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getEmail()))
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) { throw new UnsupportedOperationException("TODO"); }
    @Override
    public void logout(String refreshToken) { /* TODO: revoke token */ }
}
