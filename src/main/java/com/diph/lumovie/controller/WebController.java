package com.diph.lumovie.controller;

import com.diph.lumovie.dto.response.MovieResponse;
import com.diph.lumovie.entity.*;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.enums.Role;
import com.diph.lumovie.repository.*;
import com.diph.lumovie.service.MovieService;
import com.diph.lumovie.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final MovieService movieService;
    private final UserService userService;
    private final EpisodeRepository episodeRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;

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
       AUTH PAGES
    ══════════════════════════════════════ */
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/auth/logout")
    public String logoutPage(HttpServletResponse response) {
        // Xóa cookie accessToken
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/";
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

            // 5. Rating count
            model.addAttribute("ratingCount",
                    ratingRepository.findByMovieId(movie.getId()).size());

            // 6. Data cho user đã đăng nhập
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
            return "redirect:/";
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

        if (rating < 1 || rating > 5) rating = 1;

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Movie movie = movieRepository.findById(id).orElseThrow();

        Rating r = ratingRepository
                .findByMovieIdAndUserId(id, user.getId())
                .orElse(new Rating());
        r.setMovie(movie);
        r.setUser(user);
        r.setScore(rating);
        ratingRepository.save(r);

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

    /* ══════════════════════════════════════
       WATCH PAGE
    ══════════════════════════════════════ */
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

    /* ══════════════════════════════════════
       MOVIE LIST + FILTER
    ══════════════════════════════════════ */
    @GetMapping("/movies")
    public String listMovies(@RequestParam(required = false) String genre,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String sort,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {

        Pageable pageable = PageRequest.of(page, 20);
        Page<MovieResponse> movies = movieService.filterMovies(genre, type, sort, pageable);

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("types", MovieType.values());
        model.addAttribute("years", List.of(2026, 2025, 2024, 2023, 2022));
        model.addAttribute("pageTitle", genre != null ? "PHIM " + genre.toUpperCase() : "TẤT CẢ PHIM");

        if ("MOVIE".equals(type)) model.addAttribute("currentPage", "movie");
        else if ("SERIES".equals(type)) model.addAttribute("currentPage", "series");
        else if ("ANIME".equals(type)) model.addAttribute("currentPage", "anime");
        else if ("latest".equals(sort)) model.addAttribute("currentPage", "latest");

        return "movie/list";
    }

    /* ══════════════════════════════════════
       PROFILE PAGE
    ══════════════════════════════════════ */
    @GetMapping("/profile")
    public String profilePage(Model model, Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken)
            return "redirect:/auth/login";

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);

        // Stats
        long watchlistCount = watchlistRepository.countByUserId(user.getId());
        long historyCount = watchHistoryRepository.countByUserId(user.getId());
        long ratingCount = ratingRepository.countByUserId(user.getId());
        model.addAttribute("watchlistCount", watchlistCount);
        model.addAttribute("historyCount", historyCount);
        model.addAttribute("ratingCount", ratingCount);

        // Watchlist data
        List<Watchlist> watchlist = watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId());
        model.addAttribute("watchlist", watchlist);

        // History data
        List<WatchHistory> history = watchHistoryRepository.findByUserIdWithMovieOrderByWatchedAtDesc(user.getId());
        model.addAttribute("history", history);

        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String bio,
                                Authentication auth,
                                RedirectAttributes redirect) {
        if (auth == null) return "redirect:/auth/login";

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (fullName != null) user.setFullName(fullName.trim());
        if (bio != null) user.setBio(bio.trim());
        userRepository.save(user);

        redirect.addFlashAttribute("profileSuccess", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes redirect) {
        if (auth == null) return "redirect:/auth/login";

        if (!newPassword.equals(confirmPassword)) {
            redirect.addFlashAttribute("passwordError", "Mật khẩu xác nhận không khớp!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirect.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(auth.getName(), currentPassword, newPassword);
            redirect.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("passwordError", e.getMessage());
        }

        return "redirect:/profile";
    }

    /* ══════════════════════════════════════
       ADMIN — DASHBOARD
    ══════════════════════════════════════ */
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        // Stats
        model.addAttribute("totalMovies", movieRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalComments", commentRepository.count());

        // Tổng lượt xem
        long totalViews = movieRepository.findAll().stream()
                .mapToLong(m -> m.getViewCount() != null ? m.getViewCount() : 0)
                .sum();
        model.addAttribute("totalViews", totalViews);

        // Thống kê tháng này
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        model.addAttribute("newUsersThisMonth", userRepository.countByCreatedAtAfter(startOfMonth));
        model.addAttribute("newMoviesThisMonth", movieRepository.countByCreatedAtAfter(startOfMonth));

        // Recent movies & users
        model.addAttribute("recentMovies", movieRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
        model.addAttribute("recentUsers", userRepository.findTop10ByOrderByCreatedAtDesc());

        return "admin/dashboard";
    }

    /* ══════════════════════════════════════
       ADMIN — USER MANAGEMENT
    ══════════════════════════════════════ */
    @GetMapping("/admin/users")
    public String adminUserList(@RequestParam(required = false) String role,
                                @RequestParam(required = false) String q,
                                @RequestParam(defaultValue = "0") int page,
                                Model model) {

        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;

        if (q != null && !q.isBlank()) {
            users = userRepository.searchUsers(q, pageable);
        } else if (role != null && !role.isBlank()) {
            try {
                Role roleEnum = Role.valueOf(role);
                users = userRepository.findByRole(roleEnum, pageable);
            } catch (IllegalArgumentException e) {
                users = userRepository.findAll(pageable);
            }
        } else {
            users = userRepository.findAll(pageable);
        }

        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @PostMapping("/admin/users/{id}/toggle-role")
    @Transactional
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();

        // Cycle: ROLE_USER → ROLE_VIP → ROLE_ADMIN → ROLE_USER
        switch (user.getRole()) {
            case ROLE_USER -> user.setRole(Role.ROLE_VIP);
            case ROLE_VIP -> user.setRole(Role.ROLE_ADMIN);
            case ROLE_ADMIN -> user.setRole(Role.ROLE_USER);
        }
        userRepository.save(user);

        redirect.addFlashAttribute("success", "Đã đổi vai trò của " + user.getUsername() + " thành " + user.getRole().name());
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle-status")
    @Transactional
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String status = user.isEnabled() ? "mở khóa" : "khóa";
        redirect.addFlashAttribute("success", "Đã " + status + " tài khoản " + user.getUsername());
        return "redirect:/admin/users";
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