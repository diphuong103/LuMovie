package com.diph.lumovie.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        return new RestTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // 1. Không báo lỗi khi gặp trường lạ trong JSON
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                // 2. Không báo lỗi khi map 'null' vào kiểu primitive (int, long...)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)

                // 3. Hỗ trợ Java 8 Date/Time (LocalDate, LocalDateTime,...)
                .registerModule(new JavaTimeModule())

                // 4. (Tùy chọn) Viết ngày tháng dưới dạng ISO-8601 thay vì mảng số [2024,1,1]
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    }
}
//AppConfig là class cấu hình chung giúp ứng dụng Spring Boot giao tiếp với API bên ngoài và xử lý dữ liệu JSON một cách ổn định và linh hoạt.