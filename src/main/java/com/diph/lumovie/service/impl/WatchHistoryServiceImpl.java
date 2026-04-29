package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.request.HistorySyncRequest;
import com.diph.lumovie.dto.response.HistoryResponse;
import com.diph.lumovie.entity.Episode;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.entity.WatchHistory;
import com.diph.lumovie.repository.EpisodeRepository;
import com.diph.lumovie.repository.WatchHistoryRepository;
import com.diph.lumovie.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchHistoryServiceImpl implements WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final EpisodeRepository episodeRepository;

    @Override
    @Transactional
    public void upsertHistory(User user, Long episodeId, Long duration, Boolean isCompleted) {
        processUpsert(user, episodeId, duration, isCompleted, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchHistory> getDistinctUserHistory(Long userId) {
        List<WatchHistory> histories = watchHistoryRepository.findByUserIdWithMovieOrderByLastWatchedTimeDesc(userId);
        Set<Long> seenMovieIds = new HashSet<>();
        return histories.stream()
                .filter(h -> seenMovieIds.add(h.getEpisode().getMovie().getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryResponse> getUserHistory(Long userId) {
        List<WatchHistory> distinctHistories = getDistinctUserHistory(userId);
        return distinctHistories.stream().map(h -> HistoryResponse.builder()
                .episodeId(h.getEpisode().getId())
                .movieId(h.getEpisode().getMovie().getId())
                .movieTitle(h.getEpisode().getMovie().getTitle())
                .movieSlug(h.getEpisode().getMovie().getSlug())
                .posterUrl(h.getEpisode().getMovie().getPosterUrl())
                .episodeNumber(h.getEpisode().getEpisodeNumber())
                .duration(h.getDuration())
                .isCompleted(h.getIsCompleted())
                .lastWatchedTime(h.getLastWatchedTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncGuestHistory(User user, List<HistorySyncRequest> localHistories) {
        if (localHistories == null || localHistories.isEmpty())
            return;
        for (HistorySyncRequest req : localHistories) {
            processUpsert(user, req.getEpisodeId(), req.getDuration(), req.getIsCompleted(), req.getLastWatchedTime());
        }
    }

    private void processUpsert(User user, Long episodeId, Long duration, Boolean isCompleted, LocalDateTime watchTime) {
        if (watchTime == null)
            watchTime = LocalDateTime.now();
        Episode episode = episodeRepository.findById(episodeId).orElse(null);
        if (episode == null)
            return;

        WatchHistory existing = watchHistoryRepository.findByUserIdAndEpisodeId(user.getId(), episodeId).orElse(null);

        if (existing != null) {
            if (existing.getLastWatchedTime().isBefore(watchTime)) {
                existing.setDuration(duration);
                existing.setIsCompleted(isCompleted);
                existing.setLastWatchedTime(watchTime);
                watchHistoryRepository.save(existing);
            }
        } else {
            WatchHistory newHistory = WatchHistory.builder()
                    .user(user)
                    .episode(episode)
                    .duration(duration)
                    .isCompleted(isCompleted)
                    .lastWatchedTime(watchTime)
                    .build();
            watchHistoryRepository.save(newHistory);
        }
    }
}
