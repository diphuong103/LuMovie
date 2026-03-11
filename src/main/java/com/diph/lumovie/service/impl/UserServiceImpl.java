package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.request.UpdateProfileRequest;
import com.diph.lumovie.dto.response.UserResponse;
import com.diph.lumovie.exception.BadRequestException;
import com.diph.lumovie.exception.ResourceNotFoundException;
import com.diph.lumovie.mapper.UserMapper;
import com.diph.lumovie.repository.UserRepository;
import com.diph.lumovie.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(String email) {
        return userMapper.toResponse(userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Override @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override @Transactional
    public void changePassword(String email, String oldPass, String newPass) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(oldPass, user.getPassword()))
            throw new BadRequestException("Old password is incorrect");
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }
}
