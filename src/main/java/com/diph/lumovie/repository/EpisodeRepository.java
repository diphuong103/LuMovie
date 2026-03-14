package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    long countByMovieId(Long movieId);
    List<Episode> findByMovieIdOrderByEpisodeNumberAsc(Long movieId);
}
