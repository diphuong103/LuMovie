package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","movie_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Rating extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne @JoinColumn(name = "movie_id", nullable = false) private Movie movie;
    @Column(nullable = false) private Integer score;
}
