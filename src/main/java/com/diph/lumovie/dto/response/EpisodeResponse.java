package com.diph.lumovie.dto.response;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class EpisodeResponse {
    private Long id;
    private Integer episodeNumber;
    private Integer seasonNumber;
    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration;
    private Long viewCount;
}
