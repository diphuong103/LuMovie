package com.diph.lumovie.controller;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/movies") @RequiredArgsConstructor
@Tag(name = "Movies")
public class MovieController {
    private final MovieService movieService;
    @GetMapping @Operation(summary = "Danh sách phim")
    public ResponseEntity<ApiResponse<PageResponse<MovieResponse>>> getAll(
        @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="createdAt") String sortBy, @RequestParam(defaultValue="DESC") String dir) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getAll(PageRequest.of(page, size, Sort.Direction.valueOf(dir), sortBy))));
    }
    @GetMapping("/{id}") @Operation(summary = "Chi tiết phim theo ID")
    public ResponseEntity<ApiResponse<MovieResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getById(id)));
    }
    @GetMapping("/slug/{slug}") @Operation(summary = "Chi tiết phim theo slug")
    public ResponseEntity<ApiResponse<MovieResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getBySlug(slug)));
    }
    @GetMapping("/search") @Operation(summary = "Tìm kiếm phim")
    public ResponseEntity<ApiResponse<PageResponse<MovieResponse>>> search(
        @RequestParam String keyword, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.search(keyword, PageRequest.of(page, size))));
    }
    @GetMapping("/trending") @Operation(summary = "Phim đang hot")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getTrending() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getTrending(PageRequest.of(0, 20))));
    }
    @GetMapping("/top-rated") @Operation(summary = "Phim đánh giá cao")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getTopRated() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getTopRated()));
    }
    @GetMapping("/latest") @Operation(summary = "Phim mới nhất")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getLatest() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getLatest()));
    }
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PageResponse<MovieResponse>>> getByType(
        @PathVariable MovieType type, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getByType(type, PageRequest.of(page, size))));
    }
    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<Void>> incrementView(@PathVariable Long id) {
        movieService.incrementView(id); return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
