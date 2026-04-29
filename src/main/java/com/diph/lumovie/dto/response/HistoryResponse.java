package com.diph.lumovie.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistoryResponse {
    private Long episodeId;
    private Long movieId;
    private String movieTitle;
    private String movieSlug;
    private String posterUrl;
    private Integer episodeNumber;
    private Long duration;
    private Boolean isCompleted;
    private LocalDateTime lastWatchedTime;
}
