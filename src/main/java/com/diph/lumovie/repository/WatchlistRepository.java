package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    Optional<Watchlist> findByMovieIdAndUserId(Long movieId, Long userId);
    boolean existsByMovieIdAndUserId(Long movieId, Long userId);
}
