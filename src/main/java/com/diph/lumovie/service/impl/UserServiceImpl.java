package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.request.UpdateProfileRequest;
import com.diph.lumovie.dto.response.UserResponse;
import com.diph.lumovie.entity.User;
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

    /**
     * Tìm user bằng username (nhất quán với JWT subject = username)
     * Fallback tìm bằng email nếu không tìm thấy theo username
     */
    private User findUserByIdentifier(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + identifier));
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        return userMapper.toResponse(findUserByIdentifier(username));
    }

    @Override @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        var user = findUserByIdentifier(username);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) user.setBio(request.getBio());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override @Transactional
    public void changePassword(String username, String oldPass, String newPass) {
        var user = findUserByIdentifier(username);
        if (!passwordEncoder.matches(oldPass, user.getPassword()))
            throw new BadRequestException("Mật khẩu hiện tại không chính xác!");
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
    }
}
