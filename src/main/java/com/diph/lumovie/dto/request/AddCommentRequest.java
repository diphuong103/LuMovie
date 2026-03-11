package com.diph.lumovie.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class AddCommentRequest {
    @NotNull private Long movieId;
    private Long parentId;
    @NotBlank private String content;
}
