package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByMovieIdAndUserId(Long movieId, Long userId);
    List<Rating> findByMovieId(Long movieId);
}
