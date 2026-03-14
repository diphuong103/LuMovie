package com.diph.lumovie.dto.ophim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OPhimDetailResponse {

    private boolean status;
    private String msg;
    private MovieDetail movie;

    @JsonProperty("episodes")
    private List<ServerData> episodes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieDetail {

        @JsonProperty("_id")
        private String id;

        private String name;
        private String slug;

        @JsonProperty("origin_name")
        private String originName;

        private String content;
        private String type;
        private String status;
        private String time;
        private String quality;
        private String lang;
        private String notify;

        @JsonProperty("poster_url")
        private String posterUrl;

        @JsonProperty("thumb_url")
        private String thumbUrl;

        @JsonProperty("trailer_url")
        private String trailerUrl;

        private Integer year;  // ← Integer, not String

        @JsonProperty("episode_current")
        private String episodeCurrent;

        @JsonProperty("episode_total")
        private String episodeTotal;

        @JsonProperty("is_copyright")
        private boolean isCopyright;

        private List<CategoryItem> category;
        private List<CountryItem> country;

        // ← Plain String lists — matches how MovieSeederService uses them
        private List<String> actor;
        private List<String> director;

        @JsonProperty("view")
        private Long viewCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryItem {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryItem {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        @JsonProperty("server_name")
        private String serverName;

        @JsonProperty("server_data")
        private List<EpisodeData> serverData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EpisodeData {
        private String name;
        private String slug;
        private String filename;

        @JsonProperty("link_embed")
        private String linkEmbed;

        @JsonProperty("link_m3u8")
        private String linkM3u8;
    }
}