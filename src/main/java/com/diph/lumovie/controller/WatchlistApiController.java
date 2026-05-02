package com.diph.lumovie.controller;

import com.diph.lumovie.dto.response.ApiResponse;
import com.diph.lumovie.service.WatchlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
@Tag(name = "Watchlist")
public class WatchlistApiController {

    private final WatchlistService watchlistService;

    /**
     * Lấy danh sách movie ID mà user đã thả tim.
     * Frontend dùng API này khi load trang để highlight trái tim.
     */
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getWatchlistIds(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Long> ids = watchlistService.getWatchlistMovieIds(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(ids));
    }

    /**
     * Toggle thêm/xóa phim khỏi danh sách yêu thích.
     * Trả về trạng thái mới: added = true nếu vừa thêm, false nếu vừa xóa.
     */
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleWatchlist(
            @RequestParam Long movieId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean added = watchlistService.toggleWatchlist(userDetails.getUsername(), movieId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("added", added)));
    }
}
