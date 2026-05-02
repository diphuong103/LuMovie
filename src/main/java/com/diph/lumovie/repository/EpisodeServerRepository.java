package com.diph.lumovie.repository;

import com.diph.lumovie.entity.EpisodeServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeServerRepository extends JpaRepository<EpisodeServer, Long> {
    List<EpisodeServer> findByEpisodeIdOrderByIdAsc(Long episodeId);
}
