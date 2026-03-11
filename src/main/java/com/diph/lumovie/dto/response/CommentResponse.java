package com.diph.lumovie.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class CommentResponse {
    private Long id;
    private UserResponse user;
    private String content;
    private Integer likes;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
}
