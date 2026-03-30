package com.diph.lumovie.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6, max = 32) private String password;
    private String fullName;
}
