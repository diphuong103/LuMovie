package com.diph.lumovie.dto.request;
import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
@Data
public class CreateMovieRequest {
    @NotBlank private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private String director;
    private String actors;
    private String country;
    private String language;
    private MovieStatus status;
    private MovieType type;
    private List<Long> genreIds;
}
