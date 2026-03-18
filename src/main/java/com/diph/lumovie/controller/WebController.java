package com.diph.lumovie.controller;

import com.diph.lumovie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final MovieService movieService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("trendingMovies", movieService.getTrending());
            model.addAttribute("latestMovies",   movieService.getLatest());
            model.addAttribute("topRatedMovies", movieService.getTopRated());
            model.addAttribute("featuredMovie",  movieService.getFeatured());
            model.addAttribute("genres",         movieService.getAllGenres());
            model.addAttribute("genreColors",    genreColors());
        } catch (Exception e) {
            // Tránh crash trang, log lỗi
            e.printStackTrace();
        }
        return "index";
    }

    private List<Map<String, String>> genreColors() {
        return List.of(
                Map.of("bg","rgba(239,68,68,0.08)",   "border","rgba(239,68,68,0.2)"),
                Map.of("bg","rgba(59,130,246,0.08)",  "border","rgba(59,130,246,0.2)"),
                Map.of("bg","rgba(236,72,153,0.08)",  "border","rgba(236,72,153,0.2)"),
                Map.of("bg","rgba(34,197,94,0.08)",   "border","rgba(34,197,94,0.2)"),
                Map.of("bg","rgba(234,179,8,0.08)",   "border","rgba(234,179,8,0.2)"),
                Map.of("bg","rgba(168,85,247,0.08)",  "border","rgba(168,85,247,0.2)"),
                Map.of("bg","rgba(20,184,166,0.08)",  "border","rgba(20,184,166,0.2)")
        );
    }
}