package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    Optional<Watchlist> findByMovieIdAndUserId(Long movieId, Long userId);
    boolean existsByMovieIdAndUserId(Long movieId, Long userId);
    @Query("SELECT w FROM Watchlist w JOIN FETCH w.movie WHERE w.user.id = :userId ORDER BY w.addedAt DESC")
    List<Watchlist> findByUserIdOrderByAddedAtDesc(@Param("userId") Long userId);
    long countByUserId(Long userId);
}
