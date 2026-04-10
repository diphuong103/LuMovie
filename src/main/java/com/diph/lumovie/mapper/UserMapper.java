package com.diph.lumovie.mapper;
import com.diph.lumovie.dto.response.UserResponse;
import com.diph.lumovie.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "createdAt", target = "createdAt")
    UserResponse toResponse(User user);
}
