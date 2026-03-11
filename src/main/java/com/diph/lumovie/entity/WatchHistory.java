package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "watch_history")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WatchHistory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne @JoinColumn(name = "episode_id", nullable = false) private Episode episode;
    private Integer progressSeconds = 0;
    private Boolean completed = false;
    private LocalDateTime watchedAt = LocalDateTime.now();
}
