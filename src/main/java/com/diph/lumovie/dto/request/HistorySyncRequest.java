package com.diph.lumovie.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorySyncRequest {
    private Long episodeId;
    private Long duration;
    private Boolean isCompleted;
    private LocalDateTime lastWatchedTime;
}
