package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subtitles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subtitle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private EpisodeServer server;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false, length = 1000)
    private String subtitleUrl;
}
