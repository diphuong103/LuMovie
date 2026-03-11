package com.diph.lumovie.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("LuMovie API").version("1.0")
                .description("API cho ứng dụng xem phim LuMovie")
                .contact(new Contact().name("LuMovie Team")))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components().addSecuritySchemes("BearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
