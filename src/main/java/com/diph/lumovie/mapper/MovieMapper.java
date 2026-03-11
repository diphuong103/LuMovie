package com.diph.lumovie.mapper;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.entity.*;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieResponse toResponse(Movie movie);
    GenreResponse toGenreResponse(Genre genre);
    EpisodeResponse toEpisodeResponse(Episode episode);
}
