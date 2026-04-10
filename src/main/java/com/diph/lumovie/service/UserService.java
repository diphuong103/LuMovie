package com.diph.lumovie.service;
import com.diph.lumovie.dto.request.UpdateProfileRequest;
import com.diph.lumovie.dto.response.UserResponse;
public interface UserService {
    UserResponse getCurrentUser(String username);
    UserResponse updateProfile(String username, UpdateProfileRequest request);
    void changePassword(String username, String oldPass, String newPass);
}
