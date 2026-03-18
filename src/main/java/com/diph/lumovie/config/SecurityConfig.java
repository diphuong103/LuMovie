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
    private final JwtAuthFilter jwtAuthFilter;  // Lấy token từ header sau đó nếu hợp lệ thì đăng nhập user

    // Các trang public không cần login vẫn cho xem phim
    private static final String[] PUBLIC = {
            "/", "/index",
            "/error",
            "/dev/**", // thư mục dev
            "/css/**", "/js/**", "/images/**",  // cho phép load
            "/api/auth/**","/api/movies/**","/api/genres/**","/api/search/**",
        "/swagger-ui/**","/v3/api-docs/**","/actuator/**"  //swagger-ui (giao diện web hiển thị danh sách API và cho phép test trực tiếp).
    };
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(PUBLIC).permitAll() //(cho phép mọi người truy cập, không cần đăng nhập)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
}

//SecurityConfig là class trung tâm cấu hình Spring Security để kiểm soát đăng nhập, xác thực JWT và phân quyền truy cập các API trong hệ thống.