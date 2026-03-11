package com.diph.lumovie.mapper;
import com.diph.lumovie.dto.response.CommentResponse;
import com.diph.lumovie.entity.Comment;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper { CommentResponse toResponse(Comment comment); }
