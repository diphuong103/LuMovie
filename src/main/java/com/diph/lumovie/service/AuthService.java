package com.diph.lumovie.service;
import com.diph.lumovie.dto.request.*;
import com.diph.lumovie.dto.response.JwtResponse;
public interface AuthService {
    JwtResponse login(LoginRequest request);
    JwtResponse register(RegisterRequest request);
    JwtResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
