package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episode_servers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeServer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private String serverName;

    @Column(nullable = false, length = 1000)
    private String videoUrl;

    @Builder.Default
    private boolean isPremium = false;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtitle> subtitles = new ArrayList<>();
}
