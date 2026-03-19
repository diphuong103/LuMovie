package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "episodes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Episode extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "movie_id", nullable = false) private Movie movie;
    private Integer episodeNumber;
    @Builder.Default
    private Integer seasonNumber = 1;
    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration;
    @Builder.Default
    private Long viewCount = 0L;
}
