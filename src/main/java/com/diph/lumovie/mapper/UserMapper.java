package com.diph.lumovie.mapper;
import com.diph.lumovie.dto.response.UserResponse;
import com.diph.lumovie.entity.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface UserMapper { UserResponse toResponse(User user); }
