package com.diph.lumovie.controller;

import com.diph.lumovie.dto.request.HistorySyncRequest;
import com.diph.lumovie.dto.request.HistoryUpdateRequest;
import com.diph.lumovie.dto.response.ApiResponse;
import com.diph.lumovie.dto.response.HistoryResponse;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.repository.UserRepository;
import com.diph.lumovie.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;
    private final UserRepository userRepository;

    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody HistoryUpdateRequest request, Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<Void>builder().success(false).message("Unauthorized").build());
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            watchHistoryService.upsertHistory(user, request.getEpisodeId(), request.getDuration(),
                    request.getIsCompleted());
        }
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Updated").build());
    }

    @GetMapping
    public ResponseEntity<?> getHistory(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<Void>builder().success(false).message("Unauthorized").build());
        }
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<HistoryResponse> histories = watchHistoryService.getUserHistory(user.getId());
        return ResponseEntity.ok(
                ApiResponse.<List<HistoryResponse>>builder().success(true).message("Success").data(histories).build());
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncHistory(@RequestBody List<HistorySyncRequest> localHistories, Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<Void>builder().success(false).message("Unauthorized").build());
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            watchHistoryService.syncGuestHistory(user, localHistories);
        }
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Synced").build());
    }
}
