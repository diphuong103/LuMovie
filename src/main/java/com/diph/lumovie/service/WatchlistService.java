package com.diph.lumovie.service;

import com.diph.lumovie.dto.response.MovieResponse;
import java.util.List;

public interface WatchlistService {
    List<MovieResponse> getUserWatchlist(Long userId);

    List<Long> getWatchlistMovieIds(String username);

    boolean toggleWatchlist(String username, Long movieId);
}
