package com.diph.lumovie.service;

import com.diph.lumovie.dto.ophim.OPhimDetailResponse;
import com.diph.lumovie.dto.ophim.OPhimDetailResponse.*;
import com.diph.lumovie.dto.ophim.OPhimItem;
import com.diph.lumovie.entity.Episode;
import com.diph.lumovie.entity.Genre;
import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.repository.EpisodeRepository;
import com.diph.lumovie.repository.GenreRepository;
import com.diph.lumovie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieSeederService {

    private final OPhimService      ophimService;
    private final MovieRepository   movieRepository;
    private final GenreRepository   genreRepository;
    private final EpisodeRepository episodeRepository;

    public SeedResult seedMovies(int maxPages, boolean fetchDetail) {
        SeedResult result = new SeedResult();
        log.info("═══ SEED START — pages={} detail={} ═══", maxPages, fetchDetail);

        List<OPhimItem> items = ophimService.fetchAllMovies(maxPages);
        result.setTotal(items.size());

        for (OPhimItem item : items) {
            try {
                if (fetchDetail) seedWithDetail(item, result);
                else             seedBasic(item, result);
            } catch (Exception e) {
                log.error("Lỗi [{}]: {}", item.getSlug(), e.getMessage());
                result.addError(item.getSlug() + ": " + e.getMessage());
            }
        }

        log.info("═══ SEED DONE — created:{} skipped:{} errors:{} ═══",
                result.getCreated(), result.getSkipped(), result.getErrors().size());
        return result;
    }

    @Transactional
    public void seedBasic(OPhimItem item, SeedResult result) {
        if (movieRepository.findBySlug(item.getSlug()).isPresent()) {
            result.incrementSkipped(); return;
        }
        Movie movie = Movie.builder()
                .title(item.getName())
                .slug(item.getSlug())
                .originalTitle(item.getOriginName())
                .posterUrl(item.getPosterUrl())
                .thumbnailUrl(item.getThumbUrl())
                .releaseYear(item.getYear())
                .type(resolveTypeFromTmdb(item.getTmdb() != null ? item.getTmdb().getType() : null))
                .status(MovieStatus.COMPLETED)
                .viewCount(0L)
                .avgRating(item.getTmdb() != null && item.getTmdb().getVoteAverage() != null
                        ? item.getTmdb().getVoteAverage() : 0.0)
                .build();
        movieRepository.save(movie);
        result.incrementCreated();
        log.info("✅ Basic: {}", movie.getTitle());
    }

    @Transactional
    public void seedWithDetail(OPhimItem item, SeedResult result) {
        if (movieRepository.findBySlug(item.getSlug()).isPresent()) {
            result.incrementSkipped(); return;
        }

        OPhimDetailResponse detail = ophimService.fetchMovieDetail(item.getSlug());
        if (detail == null || detail.getMovie() == null) {
            seedBasic(item, result); return;
        }

        MovieDetail d = detail.getMovie();

        // ── actors: List<String> → join thành chuỗi ──
        String actors = null;
        if (d.getActor() != null && !d.getActor().isEmpty()) {
            actors = d.getActor().stream()
                    .filter(a -> a != null && !a.isBlank())
                    .limit(5)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(null);
            if (actors != null && actors.length() > 255)
                actors = actors.substring(0, 252) + "...";
        }

        // ── director: List<String> → lấy phần tử đầu ──
        String director = null;
        if (d.getDirector() != null && !d.getDirector().isEmpty()) {
            director = d.getDirector().stream()
                    .filter(dir -> dir != null && !dir.isBlank()
                            && !dir.equalsIgnoreCase("Đang cập nhật"))
                    .findFirst()
                    .orElse(null);
        }

        // ── country ──
        String country = (d.getCountry() != null && !d.getCountry().isEmpty())
                ? d.getCountry().get(0).getName() : null;

        Movie movie = Movie.builder()
                .title(d.getName() != null ? d.getName() : item.getName())
                .slug(d.getSlug() != null ? d.getSlug() : item.getSlug())
                .originalTitle(d.getOriginName())
                .description(cleanHtml(d.getContent()))
                .posterUrl(d.getPosterUrl() != null ? d.getPosterUrl() : item.getPosterUrl())
                .thumbnailUrl(d.getThumbUrl() != null ? d.getThumbUrl() : item.getThumbUrl())
                .backdropUrl(d.getThumbUrl())
                .trailerUrl(d.getTrailerUrl())
                .releaseYear(d.getYear() != null ? Integer.valueOf(d.getYear()) : item.getYear())
                .duration(parseDuration(d.getTime()))
                .language(d.getLang())
                .actors(actors)
                .director(director)
                .country(country)
                .type(resolveType(
                        item.getTmdb() != null ? item.getTmdb().getType() : null,
                        d.getType()))
                .status(resolveStatus(d.getStatus()))
                .viewCount(d.getViewCount() != null ? d.getViewCount() : 0L)
                .avgRating(item.getTmdb() != null && item.getTmdb().getVoteAverage() != null
                        ? item.getTmdb().getVoteAverage() : 0.0)
                .build();

        // ── genres ──
        if (d.getCategory() != null && !d.getCategory().isEmpty()) {
            List<Genre> genres = new ArrayList<>();
            for (CategoryItem cat : d.getCategory()) {
                Genre genre = genreRepository.findBySlug(cat.getSlug())
                        .orElseGet(() -> {
                            Genre g = new Genre();
                            g.setName(cat.getName());
                            g.setSlug(cat.getSlug());
                            return genreRepository.save(g);
                        });
                genres.add(genre);
            }
            movie.setGenres(genres);
        }

        Movie saved = movieRepository.save(movie);

        // ── episodes ──
        if (detail.getEpisodes() != null && !detail.getEpisodes().isEmpty()) {
            saveEpisodes(saved, detail.getEpisodes());
        }

        result.incrementCreated();
        log.info("✅ Full: {} | actors={} | episodes={}",
                saved.getTitle(), actors != null ? "✓" : "✗",
                saved.getEpisodes() != null ? saved.getEpisodes().size() : 0);

        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    }

    private void saveEpisodes(Movie movie, List<ServerData> serverList) {
        ServerData server = serverList.stream()
                .filter(s -> s.getServerData() != null && !s.getServerData().isEmpty())
                .findFirst().orElse(null);
        if (server == null) return;

        List<Episode> episodes = new ArrayList<>();
        int epNumber = 1;
        for (EpisodeData ep : server.getServerData()) {
            Episode episode = new Episode();
            episode.setMovie(movie);
            episode.setEpisodeNumber(parseEpisodeNumber(ep.getName(), epNumber));
            episode.setSeasonNumber(1);
            episode.setTitle(ep.getName());
            episode.setVideoUrl(ep.getLinkM3u8() != null && !ep.getLinkM3u8().isBlank()
                    ? ep.getLinkM3u8() : ep.getLinkEmbed());
            episodes.add(episode);
            epNumber++;
        }
        episodeRepository.saveAll(episodes);
        log.debug("  → {} tập", episodes.size());
    }

    // ── Helpers ──

    private MovieType resolveType(String tmdbType, String ophimType) {
        if (ophimType != null) {
            return switch (ophimType.toLowerCase()) {
                case "single"    -> MovieType.MOVIE;
                case "series"    -> MovieType.SERIES;
                case "hoathinh"  -> MovieType.ANIME;
                case "tvshows"   -> MovieType.TV_SHOW;
                default          -> MovieType.MOVIE;
            };
        }
        return resolveTypeFromTmdb(tmdbType);
    }

    private MovieType resolveTypeFromTmdb(String tmdbType) {
        if ("tv".equalsIgnoreCase(tmdbType))    return MovieType.SERIES;
        if ("movie".equalsIgnoreCase(tmdbType)) return MovieType.MOVIE;
        return MovieType.MOVIE;
    }

    private MovieStatus resolveStatus(String status) {
        if (status == null) return MovieStatus.COMPLETED;
        return switch (status.toLowerCase()) {
            case "ongoing"   -> MovieStatus.ONGOING;
            case "completed" -> MovieStatus.COMPLETED;
            default          -> MovieStatus.COMPLETED;
        };
    }

    private Integer parseDuration(String time) {
        if (time == null) return null;
        try {
            String digits = time.replaceAll("[^0-9]", "").trim();
            if (digits.isEmpty()) return null;
            return Integer.parseInt(digits.substring(0, Math.min(digits.length(), 3)));
        } catch (Exception e) { return null; }
    }

    private int parseEpisodeNumber(String name, int fallback) {
        if (name == null) return fallback;
        try {
            String digits = name.replaceAll("[^0-9]", "").trim();
            return digits.isEmpty() ? fallback : Integer.parseInt(digits);
        } catch (Exception e) { return fallback; }
    }

    private String cleanHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]+>", "").trim();
    }

    // ── Result ──
    public static class SeedResult {
        private int total, created, skipped;
        private final List<String> errors = new ArrayList<>();
        public void incrementCreated() { created++; }
        public void incrementSkipped() { skipped++; }
        public void addError(String msg) { errors.add(msg); }
        public void setTotal(int t)     { total = t; }
        public int getTotal()           { return total; }
        public int getCreated()         { return created; }
        public int getSkipped()         { return skipped; }
        public List<String> getErrors() { return errors; }
    }
}
