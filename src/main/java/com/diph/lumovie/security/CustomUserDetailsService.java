package com.diph.lumovie.security;

import com.diph.lumovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Tìm bằng Email, nếu không thấy thì tìm bằng Username
        var user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + identifier));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // Nên trả về Username chính thức của User
                user.getPassword() != null ? user.getPassword() : "",
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())) // Thêm ROLE_ prefix nếu cần
        );
    }
}
