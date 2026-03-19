package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    long countByMovieId(Long movieId);
    List<Episode> findByMovieIdOrderByEpisodeNumberAsc(Long movieId);

    Optional<Episode> findByMovieIdAndEpisodeNumber(Long movieId, Integer ep);
}
