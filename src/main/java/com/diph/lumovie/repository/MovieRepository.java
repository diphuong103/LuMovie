package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.enums.MovieType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findBySlug(String slug);
    Page<Movie> findByType(MovieType type, Pageable pageable);
    List<Movie> findTop10ByOrderByViewCountDesc();  // Training
    List<Movie> findTop10ByOrderByAvgRatingDesc();  // Đánh giá cao
    List<Movie> findTop10ByOrderByCreatedAtDesc();  // Mới cập nhật
//
//    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres ORDER BY m.viewCount DESC")
//    List<Movie> findTop10ByOrderByViewCountDesc();
//
//    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres ORDER BY m.avgRating DESC")
//    List<Movie> findTop10ByOrderByAvgRatingDesc();
//
//    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres ORDER BY m.createdAt DESC")
//    List<Movie> findTop10ByOrderByCreatedAtDesc();
boolean existsBySlug(String slug);



    @Query("""
    SELECT DISTINCT m FROM Movie m JOIN m.genres g
    WHERE g IN (SELECT g2 FROM Movie m2 JOIN m2.genres g2 WHERE m2.id = :movieId)
    AND m.id != :movieId
    ORDER BY m.viewCount DESC
    """)
    List<Movie> findRelated(@Param("movieId") Long movieId, Pageable pageable);

    @Query("""
    SELECT DISTINCT m FROM Movie m
    LEFT JOIN FETCH m.genres
    WHERE m.id IN (
        SELECT m2.id FROM Movie m2
        ORDER BY m2.viewCount DESC
        LIMIT 6
    )
    ORDER BY m.viewCount DESC
    """)


    // Search movie
    List<Movie> findTop6TrendingWithGenres();

    @Query(value = "SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres " +
            "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
            "OR LOWER(m.description) LIKE LOWER(CONCAT('%',:keyword,'%'))",
            countQuery = "SELECT COUNT(DISTINCT m) FROM Movie m " +
                    "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
                    "OR LOWER(m.description) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Movie> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    @Modifying
    @Query("UPDATE Movie m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(Long id);

    Page<Movie> findByGenres_Slug(String genreSlug, Pageable pageable);
}
