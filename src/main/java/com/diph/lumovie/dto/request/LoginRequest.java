package com.diph.lumovie.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LoginRequest {
    @NotBlank(message = "Username/Email không được để trống")
    private String username; // Đổi từ email thành username cho khớp với Frontend

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}