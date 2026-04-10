package com.diph.lumovie.repository;

import com.diph.lumovie.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    @Query("SELECT wh FROM WatchHistory wh JOIN FETCH wh.episode e JOIN FETCH e.movie WHERE wh.user.id = :userId ORDER BY wh.watchedAt DESC")
    List<WatchHistory> findByUserIdWithMovieOrderByWatchedAtDesc(@Param("userId") Long userId);

    long countByUserId(Long userId);
}
