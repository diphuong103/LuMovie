package com.diph.lumovie.dto.response;

import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String trailerUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private String director;
    private String actors;
    private Double avgRating;
    private Long viewCount;
    private String country;
    private String language;
    private MovieStatus status;
    private MovieType type;
    private String slug;
    private LocalDateTime createdAt;
    private List<GenreResponse> genres;
    private List<EpisodeResponse> episodes;

    // ── Phim mới: thêm vào trong 30 ngày hoặc đang chiếu ──
    public boolean isNew() {
        boolean recentlyAdded = createdAt != null
                && createdAt.isAfter(LocalDateTime.now().minusDays(30));
        boolean ongoing = status == MovieStatus.ONGOING;
        return recentlyAdded || ongoing;
    }

    // ── Phim hot: view cao và không phải phim mới ──
    public boolean isHot() {
        return viewCount != null && viewCount > 10000 && !isNew();
    }

    // ── Tổng số tập — dùng trong badge ──
    public int getTotalEpisodes() {
        if (episodes == null || episodes.isEmpty()) return 0;
        return episodes.size();
    }

    // ── Thể loại đầu tiên để hiển thị ──
    public String getFirstGenreName() {
        if (genres == null || genres.isEmpty()) return "Phim";
        return genres.get(0).getName();
    }

    // ── Label loại phim ──
    public String getTypeLabel() {
        if (type == null) return "";
        return switch (type) {
            case MOVIE   -> "Phim Lẻ";
            case SERIES  -> "Phim Bộ";
            case ANIME   -> "Anime";
            case TV_SHOW -> "TV Show";
        };
    }
}