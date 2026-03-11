package com.diph.lumovie.dto.request;
import lombok.Data;
@Data
public class UpdateProfileRequest {
    private String fullName;
    private String avatarUrl;
    private String username;
}
