package com.diph.lumovie.controller;

import com.diph.lumovie.dto.response.MovieResponse;
import com.diph.lumovie.entity.*;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.repository.*;
import com.diph.lumovie.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final MovieService movieService;
    private final EpisodeRepository episodeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final WatchlistRepository watchlistRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    /* ══════════════════════════════════════
       HOME PAGE
    ══════════════════════════════════════ */
    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("trendingMovies", movieService.getTrending(PageRequest.of(0, 20)));
            model.addAttribute("latestMovies",   movieService.getLatest());
            model.addAttribute("topRatedMovies", movieService.getTopRated());
            model.addAttribute("featuredMovie",  movieService.getFeatured());
            model.addAttribute("genres",         movieService.getAllGenres());
            model.addAttribute("genreColors",    genreColors());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }

    /* ══════════════════════════════════════
       MOVIE DETAIL PAGE
    ══════════════════════════════════════ */
    @GetMapping("/movies/{slug}")
    public String movieDetail(@PathVariable String slug,
                              Model model,
                              Authentication auth) {
        try {
            // 1. Thông tin phim
            MovieResponse movie = movieService.getBySlug(slug);
            model.addAttribute("movie", movie);

            // 2. Tăng lượt xem
            movieService.incrementView(movie.getId());

            // 3. Phim liên quan
            model.addAttribute("relatedMovies",
                    movieService.getRelated(movie.getId(), 6));

            // 4. Bình luận
            model.addAttribute("comments",
                    commentRepository.findByMovieIdOrderByCreatedAtDesc(movie.getId()));

            // 5. Rating count (không có trong DTO, tính riêng)
            model.addAttribute("ratingCount",
                    ratingRepository.findByMovieId(movie.getId()).size());

            // 5. Data cho user đã đăng nhập
            boolean loggedIn = auth != null
                    && auth.isAuthenticated()
                    && !(auth instanceof AnonymousAuthenticationToken);

            if (loggedIn) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null) {
                    model.addAttribute("userRating",
                            ratingRepository.findByMovieIdAndUserId(movie.getId(), user.getId())
                                    .map(Rating::getScore)
                                    .orElse(0));
                    model.addAttribute("inWatchlist",
                            watchlistRepository.existsByMovieIdAndUserId(movie.getId(), user.getId()));
                } else {
                    model.addAttribute("userRating", 0);
                    model.addAttribute("inWatchlist", false);
                }
            } else {
                model.addAttribute("userRating", 0);
                model.addAttribute("inWatchlist", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";   // fallback về trang chủ nếu slug không tồn tại
        }

        return "movie/detail";
    }

    /* ══════════════════════════════════════
       POST: Bình luận
    ══════════════════════════════════════ */
    @PostMapping("/movies/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @RequestParam(required = false) String redirect,
                             Authentication auth) {
        if (auth == null) return "redirect:/auth/login";

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Movie movie = movieRepository.findById(id).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setMovie(movie);
        comment.setContent(content.trim());
        commentRepository.save(comment);

        // Redirect về trang watch nếu comment từ trang watch
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/movies/" + movie.getSlug() + "#comments";
    }

    /* ══════════════════════════════════════
       POST: Đánh giá
    ══════════════════════════════════════ */
    @PostMapping("/movies/{id}/rate")
    public String rateMovie(@PathVariable Long id,
                            @RequestParam int rating,
                            Authentication auth) {

        if (auth == null || auth instanceof AnonymousAuthenticationToken)
            return "redirect:/auth/login";

        if (rating < 1 || rating > 5) rating = 1;   // guard

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Movie movie = movieRepository.findById(id).orElseThrow();

        Rating r = ratingRepository
                .findByMovieIdAndUserId(id, user.getId())
                .orElse(new Rating());
        r.setMovie(movie);
        r.setUser(user);
        r.setScore(rating);
        ratingRepository.save(r);

        // Tính lại avgRating
        double avg = ratingRepository.findByMovieId(id)
                .stream().mapToInt(Rating::getScore)
                .average().orElse(0.0);
        movie.setAvgRating(Math.round(avg * 10.0) / 10.0);
        movieRepository.save(movie);

        return "redirect:/movies/" + movie.getSlug() + "#rating";
    }

    /* ══════════════════════════════════════
       POST: Toggle Watchlist
    ══════════════════════════════════════ */
    @PostMapping("/watchlist/toggle")
    public String toggleWatchlist(@RequestParam Long movieId,
                                  Authentication auth,
                                  HttpServletRequest request) {

        if (auth == null || auth instanceof AnonymousAuthenticationToken)
            return "redirect:/auth/login";

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        watchlistRepository.findByMovieIdAndUserId(movieId, user.getId())
                .ifPresentOrElse(
                        watchlistRepository::delete,
                        () -> {
                            Watchlist w = new Watchlist();
                            w.setMovie(movie);
                            w.setUser(user);
                            watchlistRepository.save(w);
                        }
                );

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/movies/{slug}/watch")
    @Transactional
    public String watchMovie(@PathVariable String slug,
                             @RequestParam(defaultValue = "1") int ep,
                             Model model) {

        MovieResponse movie = movieService.getBySlug(slug);
        model.addAttribute("movie", movie);

        List<Episode> allEpisodes = episodeRepository
                .findByMovieIdOrderByEpisodeNumberAsc(movie.getId());
        model.addAttribute("allEpisodes", allEpisodes);

        Episode currentEpisode = allEpisodes.stream()
                .filter(e -> e.getEpisodeNumber() != null && e.getEpisodeNumber() == ep)
                .findFirst()
                .orElse(allEpisodes.isEmpty() ? null : allEpisodes.get(0));
        model.addAttribute("currentEpisode", currentEpisode);

        if (currentEpisode != null) {
            int cur = currentEpisode.getEpisodeNumber();
            model.addAttribute("prevEpisode", allEpisodes.stream()
                    .filter(e -> e.getEpisodeNumber() == cur - 1).findFirst().orElse(null));
            model.addAttribute("nextEpisode", allEpisodes.stream()
                    .filter(e -> e.getEpisodeNumber() == cur + 1).findFirst().orElse(null));
        } else {
            model.addAttribute("prevEpisode", null);
            model.addAttribute("nextEpisode", null);
        }

        model.addAttribute("comments",
                commentRepository.findByMovieIdOrderByCreatedAtDesc(movie.getId()));

        model.addAttribute("recommended", movieService.getRelated(movie.getId(), 6));
        movieService.incrementView(movie.getId());

        // Label cho episode
        String epLabel = currentEpisode != null
                ? "Tập " + currentEpisode.getEpisodeNumber() : "Phim Lẻ";
        model.addAttribute("epLabel", epLabel);

        return "movie/watch";
    }


    /* ══════════════════════════════════════
      SEARCH
   ══════════════════════════════════════ */
    @GetMapping("/search")
    public String searchMovie(@RequestParam(name = "q", required = false) String query,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              Authentication auth) {

        model.addAttribute("types", AccessType.Type.values());

        if (query != null && !query.isBlank()) {
            Pageable pageable = PageRequest.of(page, 10);
            Page<MovieResponse> searchResults = movieService.searchPage(query, pageable);

            model.addAttribute("movies",       searchResults.getContent());
            model.addAttribute("totalItems",   searchResults.getTotalElements());
            model.addAttribute("totalPages",   searchResults.getTotalPages());
            model.addAttribute("currentPage",  page);
            model.addAttribute("hasPrev",      page > 0);
            model.addAttribute("hasNext",      page < searchResults.getTotalPages() - 1);
        } else {

            Page<MovieResponse> trending = (Page<MovieResponse>) movieService.getTrending(PageRequest.of(0, 20));
            model.addAttribute("movies", trending.getContent());
        }

        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("user", auth.getPrincipal());
        }

        return "movie/search";

    }

    @GetMapping("/movies")
    public String listMovies(@RequestParam(required = false) String genre,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String sort,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {

        Pageable pageable = PageRequest.of(page, 20);

        // Truyền đủ các filter vào service
        Page<MovieResponse> movies = movieService.filterMovies(genre, type, sort, pageable);

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("types", MovieType.values());
        model.addAttribute("years", List.of(2026, 2025, 2024, 2023, 2022));
        model.addAttribute("pageTitle", genre != null ? "PHIM " + genre.toUpperCase() : "TẤT CẢ PHIM");

        return "movie/list";
    }

    /* ══════════════════════════════════════
       HELPERS
    ══════════════════════════════════════ */
    private String resolveSlugRedirect(Long movieId) {
        return movieRepository.findById(movieId)
                .map(m -> "/movies/" + m.getSlug())
                .orElse("/");
    }

    private List<Map<String, String>> genreColors() {
        return List.of(
                Map.of("bg", "rgba(239,68,68,0.08)",  "border", "rgba(239,68,68,0.2)"),
                Map.of("bg", "rgba(59,130,246,0.08)", "border", "rgba(59,130,246,0.2)"),
                Map.of("bg", "rgba(236,72,153,0.08)", "border", "rgba(236,72,153,0.2)"),
                Map.of("bg", "rgba(34,197,94,0.08)",  "border", "rgba(34,197,94,0.2)"),
                Map.of("bg", "rgba(234,179,8,0.08)",  "border", "rgba(234,179,8,0.2)"),
                Map.of("bg", "rgba(168,85,247,0.08)", "border", "rgba(168,85,247,0.2)"),
                Map.of("bg", "rgba(20,184,166,0.08)", "border", "rgba(20,184,166,0.2)")
        );
    }
}