package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table (name = "watchlists", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","movie_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Watchlist extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne @JoinColumn(name = "movie_id", nullable = false) private Movie movie;
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
