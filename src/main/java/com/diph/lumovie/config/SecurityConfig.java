package com.diph.lumovie.config;

import com.diph.lumovie.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;  // Lấy token từ cookie/header → nếu hợp lệ thì đăng nhập user

    // Các trang public không cần login vẫn cho xem phim
    private static final String[] PUBLIC = {
            "/", "/index",
            "/movies/**",
            "/genres/**",
            "/error",
            "/search",
            "/list",
            "/dev/**",
            "/css/**", "/js/**", "/images/**",
            "/auth/**",
            "/api/auth/**","/api/movies/**","/api/genres/**","/api/search/**","/api/watch/**",
            "/swagger-ui/**","/v3/api-docs/**","/actuator/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(PUBLIC).permitAll()
                // Admin pages + API
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers("/api/auth/me").authenticated()
                // Tất cả còn lại cần đăng nhập (profile, watchlist, etc.)
                .anyRequest().authenticated())
            .logout(l -> l
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("accessToken")
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
}

//SecurityConfig là class trung tâm cấu hình Spring Security để kiểm soát đăng nhập, xác thực JWT và phân quyền truy cập các API trong hệ thống.