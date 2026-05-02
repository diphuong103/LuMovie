package com.diph.lumovie.controller;

import com.diph.lumovie.entity.*;
import com.diph.lumovie.enums.MovieStatus;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.enums.Role;
import com.diph.lumovie.repository.*;
import com.diph.lumovie.service.MovieSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWebController {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final GenreRepository genreRepository;
    private final EpisodeRepository episodeRepository;
    private final MovieSeederService movieSeederService;

    // ═══════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════
    @GetMapping({ "", "/dashboard" })
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        model.addAttribute("totalMovies", movieRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalViews", movieRepository.sumTotalViews());
        model.addAttribute("totalComments", commentRepository.count());

        model.addAttribute("newMoviesThisMonth", movieRepository.countByCreatedAtAfter(monthStart));
        model.addAttribute("newUsersThisMonth", userRepository.countByCreatedAtAfter(monthStart));

        // Recent movies (last 10) — eager fetch genres
        model.addAttribute("recentMovies",
                movieRepository.findRecentWithGenres(PageRequest.of(0, 10)));

        // Recent users (last 10)
        model.addAttribute("recentUsers",
                userRepository.findTop10ByOrderByCreatedAtDesc());

        return "admin/dashboard";
    }

    // ═══════════════════════════════════════════
    // MOVIE MANAGEMENT
    // ═══════════════════════════════════════════
    @GetMapping("/movies")
    @Transactional(readOnly = true)
    public String movieList(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            Model model) {

        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Movie> movies;

        if (q != null && !q.isBlank()) {
            movies = movieRepository.searchByTitleWithGenres(q, pageable);
        } else {
            movies = movieRepository.findAllWithGenres(pageable);
        }

        model.addAttribute("movies", movies);
        model.addAttribute("adminPage", "movies");
        return "admin/movie-list";
    }

    @GetMapping("/movies/create")
    @Transactional(readOnly = true)
    public String createMovieForm(Model model) {
        model.addAttribute("movie", null);
        model.addAttribute("allGenres", genreRepository.findAll());
        model.addAttribute("adminPage", "movie-create");
        return "admin/movie-form";
    }

    @PostMapping("/movies/create")
    @Transactional
    public String createMovie(@RequestParam String title,
            @RequestParam String slug,
            @RequestParam(required = false) String originalTitle,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String country,
            @RequestParam(required = false, name = "cast") String actors,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(required = false) String bannerUrl,
            @RequestParam(required = false) String trailerUrl,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(defaultValue = "MOVIE") String type,
            @RequestParam(defaultValue = "COMPLETED") String status,
            @RequestParam(required = false) List<Long> genreIds,
            RedirectAttributes redirectAttributes) {
        try {
            if (movieRepository.existsBySlug(slug)) {
                redirectAttributes.addFlashAttribute("error", "Slug '" + slug + "' đã tồn tại!");
                return "redirect:/admin/movies/create";
            }

            Movie movie = Movie.builder()
                    .title(title)
                    .slug(slug)
                    .originalTitle(originalTitle)
                    .description(description)
                    .director(director)
                    .country(country)
                    .actors(actors)
                    .releaseYear(releaseYear)
                    .duration(duration)
                    .language(language)
                    .posterUrl(posterUrl)
                    .thumbnailUrl(bannerUrl)
                    .backdropUrl(bannerUrl)
                    .trailerUrl(trailerUrl)
                    .type(MovieType.valueOf(type))
                    .status(MovieStatus.valueOf(status))
                    .viewCount(0L)
                    .avgRating(0.0)
                    .build();

            if (genreIds != null && !genreIds.isEmpty()) {
                List<Genre> genres = genreRepository.findAllById(genreIds);
                movie.setGenres(new ArrayList<>(genres));
            }

            // Single movie → save videoUrl to Episode with ep=1
            Movie saved = movieRepository.save(movie);

            if (videoUrl != null && !videoUrl.isBlank()) {
                Episode ep = Episode.builder()
                        .movie(saved)
                        .episodeNumber(1)
                        .title("Full")
                        .videoUrl(videoUrl)
                        .build();
                episodeRepository.save(ep);
            }

            redirectAttributes.addFlashAttribute("success", "Đã thêm phim: " + title);
            return "redirect:/admin/movies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies/create";
        }
    }

    @GetMapping("/movies/{id}/edit")
    @Transactional(readOnly = true)
    public String editMovieForm(@PathVariable Long id, Model model) {
        Movie movie = movieRepository.findById(id).orElseThrow();
        model.addAttribute("movie", movie);
        model.addAttribute("allGenres", genreRepository.findAll());
        model.addAttribute("adminPage", "movies");
        return "admin/movie-form";
    }

    @PostMapping("/movies/{id}/edit")
    @Transactional
    public String editMovie(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String slug,
            @RequestParam(required = false) String originalTitle,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String country,
            @RequestParam(required = false, name = "cast") String actors,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(required = false) String bannerUrl,
            @RequestParam(required = false) String trailerUrl,
            @RequestParam(defaultValue = "MOVIE") String type,
            @RequestParam(defaultValue = "COMPLETED") String status,
            @RequestParam(required = false) List<Long> genreIds,
            RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieRepository.findById(id).orElseThrow();
            movie.setTitle(title);
            movie.setSlug(slug);
            movie.setOriginalTitle(originalTitle);
            movie.setDescription(description);
            movie.setDirector(director);
            movie.setCountry(country);
            movie.setActors(actors);
            movie.setReleaseYear(releaseYear);
            movie.setDuration(duration);
            movie.setLanguage(language);
            movie.setPosterUrl(posterUrl);
            movie.setThumbnailUrl(bannerUrl);
            movie.setBackdropUrl(bannerUrl);
            movie.setTrailerUrl(trailerUrl);
            movie.setType(MovieType.valueOf(type));
            movie.setStatus(MovieStatus.valueOf(status));

            if (genreIds != null) {
                List<Genre> genres = genreRepository.findAllById(genreIds);
                movie.setGenres(new ArrayList<>(genres));
            }

            movieRepository.save(movie);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật: " + title);
            return "redirect:/admin/movies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies/" + id + "/edit";
        }
    }

    @PostMapping("/movies/{id}/delete")
    @Transactional
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieRepository.findById(id).orElseThrow();
            movieRepository.delete(movie);
            redirectAttributes.addFlashAttribute("success", "Đã xóa phim: " + movie.getTitle());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }

    // ═══════════════════════════════════════════
    // SEED DATA
    // ═══════════════════════════════════════════
    @PostMapping("/seed")
    public String seedMovies(@RequestParam(defaultValue = "1") int pages,
            RedirectAttributes redirectAttributes) {
        try {
            MovieSeederService.SeedResult result = movieSeederService.seedMovies(pages, true);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Seed hoàn tất! Tạo: %d | Bỏ qua: %d | Lỗi: %d",
                            result.getCreated(), result.getSkipped(), result.getErrors().size()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Seed thất bại: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ═══════════════════════════════════════════
    // USER MANAGEMENT
    // ═══════════════════════════════════════════
    @GetMapping("/users")
    public String userList(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;

        if (q != null && !q.isBlank()) {
            users = userRepository.searchUsers(q, pageable);
        } else if (role != null && !role.isBlank()) {
            users = userRepository.findByRole(Role.valueOf(role), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        model.addAttribute("users", users);
        model.addAttribute("adminPage", "users");
        return "admin/user-list";
    }

    @PostMapping("/users/{id}/toggle-role")
    @Transactional
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        // Cycle: USER -> VIP -> ADMIN -> USER
        Role newRole = switch (user.getRole()) {
            case ROLE_USER -> Role.ROLE_VIP;
            case ROLE_VIP -> Role.ROLE_ADMIN;
            case ROLE_ADMIN -> Role.ROLE_USER;
        };
        user.setRole(newRole);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success",
                "Đã đổi vai trò " + user.getUsername() + " → " + newRole);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    @Transactional
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success",
                user.getUsername() + " → " + (user.isEnabled() ? "Hoạt động" : "Đã khóa"));
        return "redirect:/admin/users";
    }

    // ═══════════════════════════════════════════
    // COMMENT MANAGEMENT
    // ═══════════════════════════════════════════
    @GetMapping("/comments")
    @Transactional(readOnly = true)
    public String commentList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 30, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("comments", commentRepository.findAll(pageable));
        model.addAttribute("adminPage", "comments");
        return "admin/comment-list";
    }

    @PostMapping("/comments/{id}/delete")
    @Transactional
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        commentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa bình luận #" + id);
        return "redirect:/admin/comments";
    }

    // ═══════════════════════════════════════════
    // GENRE MANAGEMENT
    // ═══════════════════════════════════════════
    @GetMapping("/genres")
    public String genreList(Model model) {
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("adminPage", "genres");
        return "admin/genre-list";
    }

    @PostMapping("/genres/create")
    @Transactional
    public String createGenre(@RequestParam String name,
            @RequestParam String slug,
            RedirectAttributes redirectAttributes) {
        Genre genre = new Genre();
        genre.setName(name);
        genre.setSlug(slug);
        genreRepository.save(genre);
        redirectAttributes.addFlashAttribute("success", "Đã thêm thể loại: " + name);
        return "redirect:/admin/genres";
    }

    @PostMapping("/genres/{id}/delete")
    @Transactional
    public String deleteGenre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        genreRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa thể loại");
        return "redirect:/admin/genres";
    }
}
