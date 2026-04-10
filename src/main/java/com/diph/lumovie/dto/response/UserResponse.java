package com.diph.lumovie.dto.response;
import com.diph.lumovie.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private Role role;
    private LocalDateTime createdAt;
}
