package com.diph.lumovie.controller;

import com.diph.lumovie.dto.ophim.OPhimDetailResponse;
import com.diph.lumovie.dto.ophim.OPhimItem;
import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.repository.MovieRepository;
import com.diph.lumovie.service.MovieSeederService;
import com.diph.lumovie.service.MovieSeederService.SeedResult;
import com.diph.lumovie.service.OPhimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dev/seed")
@RequiredArgsConstructor
public class SeederController {

    private final MovieSeederService seederService;
    private final MovieRepository    movieRepository;
    private final OPhimService       ophimService;

    // ── Ping test ──
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status",  "ok",
                "message", "SeederController hoạt động!",
                "movies",  String.valueOf(movieRepository.count())
        ));
    }

    // ── Seed nhanh ──
    @GetMapping("/quick")
    public ResponseEntity<Map<String, Object>> seedQuick(
            @RequestParam(defaultValue = "3") int pages) {
        log.info("=== SEED QUICK: {} trang ===", pages);
        SeedResult result = seederService.seedMovies(pages, false);
        return ResponseEntity.ok(Map.of(
                "status",  "done",
                "total",   result.getTotal(),
                "created", result.getCreated(),
                "skipped", result.getSkipped(),
                "errors",  result.getErrors().size(),
                "message", "Seed " + result.getCreated() + " phim thành công!"
        ));
    }

    // ── Seed đầy đủ ──
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> seedFull(
            @RequestParam(defaultValue = "2") int pages) {
        log.info("=== SEED FULL: {} trang ===", pages);
        SeedResult result = seederService.seedMovies(pages, true);
        return ResponseEntity.ok(Map.of(
                "status",  "done",
                "total",   result.getTotal(),
                "created", result.getCreated(),
                "skipped", result.getSkipped(),
                "errors",  result.getErrors(),
                "message", "Seed " + result.getCreated() + " phim đầy đủ!"
        ));
    }

    // ── Seed 1 phim theo slug ──
    @GetMapping("/one")
    public ResponseEntity<Map<String, Object>> seedOne(@RequestParam String slug) {
        log.info("=== SEED 1 PHIM: {} ===", slug);
        SeedResult result = new SeedResult();
        OPhimItem item = new OPhimItem();
        item.setSlug(slug);
        item.setName(slug);
        seederService.seedWithDetail(item, result);
        return ResponseEntity.ok(Map.of(
                "status",  result.getCreated() > 0 ? "done" : "error",
                "slug",    slug,
                "created", result.getCreated(),
                "errors",  result.getErrors()
        ));
    }

    // ── Cập nhật đầy đủ thông tin cho phim đã seed bằng quick ──
    // Map đủ: actors, director, description, backdrop_url,
    //         trailer_url, duration, language, country
    @GetMapping("/update-details")
    public ResponseEntity<Map<String, Object>> updateDetails(
            @RequestParam(defaultValue = "0") int skip) {

        log.info("=== UPDATE DETAILS (skip={}) ===", skip);
        List<Movie> movies = movieRepository.findAll();
        int updated = 0, failed = 0;

        for (int i = skip; i < movies.size(); i++) {
            Movie movie = movies.get(i);

            // Bỏ qua nếu đã có description
            if (movie.getDescription() != null && !movie.getDescription().isBlank()) {
                log.debug("Bỏ qua (đã có detail): {}", movie.getSlug());
                continue;
            }

            try {
                OPhimDetailResponse detail = ophimService.fetchMovieDetail(movie.getSlug());
                if (detail == null || detail.getMovie() == null) { failed++; continue; }

                OPhimDetailResponse.MovieDetail d = detail.getMovie();

                // description
                if (d.getContent() != null) {
                    movie.setDescription(d.getContent().replaceAll("<[^>]+>", "").trim());
                }

                // actors — tối đa 5 người, cắt nếu > 255 ký tự
                if (d.getActor() != null && !d.getActor().isEmpty()) {
                    String actors = d.getActor().stream()
                            .filter(a -> a != null && !a.isBlank())
                            .limit(5)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(null);
                    if (actors != null && actors.length() > 255)
                        actors = actors.substring(0, 252) + "...";
                    movie.setActors(actors);
                }

                // director
                if (d.getDirector() != null && !d.getDirector().isEmpty()) {
                    movie.setDirector(d.getDirector().stream()
                            .filter(dir -> dir != null && !dir.isBlank()
                                    && !dir.equalsIgnoreCase("Đang cập nhật"))
                            .findFirst()
                            .orElse(null));
                }

                // country
                if (d.getCountry() != null && !d.getCountry().isEmpty()) {
                    movie.setCountry(d.getCountry().get(0).getName());
                }

                // language
                if (d.getLang() != null) movie.setLanguage(d.getLang());

                // duration — "45 phút/tập" → 45
                if (d.getTime() != null) {
                    try {
                        String digits = d.getTime().replaceAll("[^0-9]", "").trim();
                        if (!digits.isEmpty())
                            movie.setDuration(Integer.parseInt(
                                    digits.substring(0, Math.min(digits.length(), 3))));
                    } catch (Exception ignored) {}
                }

                // trailer_url
                if (d.getTrailerUrl() != null && !d.getTrailerUrl().isBlank()) {
                    movie.setTrailerUrl(d.getTrailerUrl());
                }

                // backdrop_url — OPhim dùng thumb_url làm backdrop
                if (d.getThumbUrl() != null) {
                    movie.setBackdropUrl(d.getThumbUrl());
                }

                // poster_url
                if (d.getPosterUrl() != null && movie.getPosterUrl() == null) {
                    movie.setPosterUrl(d.getPosterUrl());
                }

                // thumbnail_url
                if (d.getThumbUrl() != null && movie.getThumbnailUrl() == null) {
                    movie.setThumbnailUrl(d.getThumbUrl());
                }

                movieRepository.save(movie);
                updated++;
                log.info("[{}/{}] ✅ {}", i + 1, movies.size(), movie.getTitle());

            } catch (Exception e) {
                log.error("Lỗi update {}: {}", movie.getSlug(), e.getMessage());
                failed++;
            }

            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }

        return ResponseEntity.ok(Map.of(
                "status",  "done",
                "total",   movies.size(),
                "updated", updated,
                "failed",  failed,
                "message", "Cập nhật " + updated + " phim thành công!"
        ));
    }
}