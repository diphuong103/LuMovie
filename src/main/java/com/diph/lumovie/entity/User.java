package com.diph.lumovie.entity;

import com.diph.lumovie.enums.AuthProvider;
import com.diph.lumovie.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(unique = true, nullable = false) private String email;
    private String password;
    private String fullName;
    private String avatarUrl;
    @Enumerated(EnumType.STRING) private Role role = Role.ROLE_USER;
    @Enumerated(EnumType.STRING) private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    private boolean isActive = true;
    private boolean emailVerified = false;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) private List<WatchHistory> watchHistories;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) private List<Watchlist> watchlist;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) private List<Rating> ratings;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) private List<Comment> comments;
}
