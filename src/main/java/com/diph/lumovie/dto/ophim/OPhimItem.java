package com.diph.lumovie.dto.ophim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// ═══════════════════════════════════════════════════
// OPhimItem — một phim trong danh sách
// ═══════════════════════════════════════════════════
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OPhimItem {

    @JsonProperty("_id")
    private String id;

    private String name;
    private String slug;

    @JsonProperty("origin_name")
    private String originName;

    @JsonProperty("poster_url")
    private String posterUrl;

    @JsonProperty("thumb_url")
    private String thumbUrl;

    private Integer year;

    private TmdbInfo tmdb;
    private ImdbInfo imdb;
    private ModifiedInfo modified;

    // ── Inner classes ──

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbInfo {
        private String type;   // "tv" | "movie" | null
        private String id;
        private Integer season;

        @JsonProperty("vote_average")
        private Double voteAverage;

        @JsonProperty("vote_count")
        private Integer voteCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImdbInfo {
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModifiedInfo {
        private String time;
    }
}
