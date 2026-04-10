package com.diph.lumovie.repository;

import com.diph.lumovie.entity.User;
import com.diph.lumovie.enums.AuthProvider;
import com.diph.lumovie.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    // Admin: phân trang với filter
    Page<User> findByRole(Role role, Pageable pageable);

    // Admin: search
    @Query("SELECT u FROM User u WHERE u.username LIKE %:q% OR u.email LIKE %:q% OR u.fullName LIKE %:q%")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);

    // Admin: stats
    long countByCreatedAtAfter(LocalDateTime dateTime);

    // Admin: recent users
    List<User> findTop10ByOrderByCreatedAtDesc();
}
