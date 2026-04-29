package com.diph.lumovie.repository;

import com.diph.lumovie.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    /**
     * FIX: JOIN FETCH cả episode VÀ episode.movie để tránh
     * LazyInitializationException
     * khi Thymeleaf truy cập item.episode.movie.posterUrl, item.episode.movie.slug,
     * v.v.
     */
    @Query("""
            SELECT wh FROM WatchHistory wh
            JOIN FETCH wh.episode e
            JOIN FETCH e.movie
            WHERE wh.user.id = :userId
            ORDER BY wh.lastWatchedTime DESC
            """)
    List<WatchHistory> findByUserIdWithMovieOrderByLastWatchedTimeDesc(@Param("userId") Long userId);

    /**
     * FIX: cần thêm method này để WebController có thể upsert watch history
     * (cập nhật lastWatchedTime nếu user đã xem tập này rồi, thay vì tạo bản ghi
     * trùng lặp)
     */
    Optional<WatchHistory> findByUserIdAndEpisodeId(Long userId, Long episodeId);

    long countByUserId(Long userId);
}