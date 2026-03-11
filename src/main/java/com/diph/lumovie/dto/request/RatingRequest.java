package com.diph.lumovie.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RatingRequest {
    @NotNull private Long movieId;
    @NotNull @Min(1) @Max(10) private Integer score;
}
