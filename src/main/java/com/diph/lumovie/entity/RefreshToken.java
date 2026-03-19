package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "refresh_tokens")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(unique = true, nullable = false, length = 512) private String token;
    private LocalDateTime expiresAt;
    @Builder.Default
    private boolean revoked = false;
}
