package com.diph.lumovie.dto.response;
import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import lombok.*;
import java.util.List;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private String director;
    private Double avgRating;
    private Long viewCount;
    private String country;
    private MovieStatus status;
    private MovieType type;
    private String slug;
    private List<GenreResponse> genres;
    private List<EpisodeResponse> episodes;
}
