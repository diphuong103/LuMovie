package com.diph.lumovie.service;

import com.diph.lumovie.dto.request.HistorySyncRequest;
import com.diph.lumovie.dto.response.HistoryResponse;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.entity.WatchHistory;

import java.util.List;

public interface WatchHistoryService {
    void upsertHistory(User user, Long episodeId, Long duration, Boolean isCompleted);

    List<HistoryResponse> getUserHistory(Long userId);

    List<WatchHistory> getDistinctUserHistory(Long userId);

    void syncGuestHistory(User user, List<HistorySyncRequest> localHistories);
}
