package com.diph.lumovie.dto.response;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class GenreResponse {
    private Long id;
    private String name;
    private String slug;
    private String icon;
}
