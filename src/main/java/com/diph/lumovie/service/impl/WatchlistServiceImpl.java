package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.response.MovieResponse;
import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.entity.User;
import com.diph.lumovie.entity.Watchlist;
import com.diph.lumovie.mapper.MovieMapper;
import com.diph.lumovie.repository.MovieRepository;
import com.diph.lumovie.repository.UserRepository;
import com.diph.lumovie.repository.WatchlistRepository;
import com.diph.lumovie.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final MovieMapper movieMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getUserWatchlist(Long userId) {
        List<Watchlist> watchlists = watchlistRepository.findWithMovieAndGenresByUserId(userId);
        return watchlists.stream()
                .map(Watchlist::getMovie)
                .map(movieMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWatchlistMovieIds(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return watchlistRepository.findMovieIdsByUserId(user.getId());
    }

    @Override
    @Transactional
    public boolean toggleWatchlist(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();

        return watchlistRepository.findByMovieIdAndUserId(movieId, user.getId())
                .map(existing -> {
                    watchlistRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    Movie movie = movieRepository.findById(movieId).orElseThrow();
                    Watchlist w = new Watchlist();
                    w.setMovie(movie);
                    w.setUser(user);
                    watchlistRepository.save(w);
                    return true;
                });
    }
}
