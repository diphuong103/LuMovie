package com.diph.lumovie.dto.response;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UserResponse user;
}
