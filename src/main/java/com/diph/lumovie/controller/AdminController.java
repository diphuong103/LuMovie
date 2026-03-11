package com.diph.lumovie.controller;
import com.diph.lumovie.dto.request.CreateMovieRequest;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.service.MovieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") @Tag(name = "Admin")
public class AdminController {
    private final MovieService movieService;
    @PostMapping("/movies")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@Valid @RequestBody CreateMovieRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Movie created", movieService.create(req)));
    }
    @PutMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(@PathVariable Long id, @Valid @RequestBody CreateMovieRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Movie updated", movieService.update(id, req)));
    }
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        movieService.delete(id); return ResponseEntity.ok(ApiResponse.ok("Movie deleted", null));
    }
}
