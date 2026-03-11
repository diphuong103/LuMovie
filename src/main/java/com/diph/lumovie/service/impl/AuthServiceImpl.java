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
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return JwtResponse.builder()
            .accessToken(jwtUtil.generateToken(user.getEmail()))
            .tokenType("Bearer")
            .user(userMapper.toResponse(user)).build();
    }

    @Override @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateEmailException(request.getEmail());
        if (userRepository.existsByUsername(request.getUsername())) throw new BadRequestException("Username already taken");
        User user = User.builder()
            .username(request.getUsername()).email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName()).build();
        userRepository.save(user);
        return JwtResponse.builder()
            .accessToken(jwtUtil.generateToken(user.getEmail()))
            .tokenType("Bearer")
            .user(userMapper.toResponse(user)).build();
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) { throw new UnsupportedOperationException("TODO"); }
    @Override
    public void logout(String refreshToken) { /* TODO: revoke token */ }
}
