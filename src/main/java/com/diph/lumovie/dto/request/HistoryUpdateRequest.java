package com.diph.lumovie.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryUpdateRequest {
    private Long episodeId;
    private Long duration;
    private Boolean isCompleted;
}
