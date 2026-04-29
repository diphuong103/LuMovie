package com.diph.lumovie.repository;

import com.diph.lumovie.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByNameIgnoreCase(String name);
}
