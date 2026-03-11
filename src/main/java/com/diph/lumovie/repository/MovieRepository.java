package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.enums.MovieType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findBySlug(String slug);
    Page<Movie> findByType(MovieType type, Pageable pageable);
    List<Movie> findTop10ByOrderByViewCountDesc();
    List<Movie> findTop10ByOrderByAvgRatingDesc();
    List<Movie> findTop10ByOrderByCreatedAtDesc();
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    @Modifying
    @Query("UPDATE Movie m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(Long id);
}
