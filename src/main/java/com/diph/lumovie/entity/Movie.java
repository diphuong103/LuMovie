package com.diph.lumovie.entity;

import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "movies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String originalTitle;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String trailerUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private String director;
    private String actors;
    @Builder.Default
    private Double avgRating = 0.0;
    @Builder.Default
    private Long viewCount = 0L;
    private String country;
    private String language;
    private String quality;
    private String slug;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MovieStatus status = MovieStatus.COMPLETED;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MovieType type = MovieType.MOVIE;
    @ManyToMany
    @JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @BatchSize(size = 20)
    private List<Genre> genres;
    @ManyToMany
    @JoinTable(name = "movie_actors", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "actor_id"))
    @BatchSize(size = 20)
    private Set<Actor> actorLinks;
    @ManyToMany
    @JoinTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "director_id"))
    @BatchSize(size = 20)
    private Set<Director> directorLinks;
    @ManyToMany
    @JoinTable(name = "movie_countries", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "country_id"))
    @BatchSize(size = 20)
    private Set<Country> countryLinks;
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Episode> episodes;
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Rating> ratings;
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Comment> comments;
}
