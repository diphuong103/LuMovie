package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByNameIgnoreCase(String name);
}
