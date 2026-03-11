package com.diph.lumovie.service;
import com.diph.lumovie.dto.request.UpdateProfileRequest;
import com.diph.lumovie.dto.response.UserResponse;
public interface UserService {
    UserResponse getCurrentUser(String email);
    UserResponse updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, String oldPass, String newPass);
}
