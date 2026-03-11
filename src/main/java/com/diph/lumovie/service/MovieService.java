package com.diph.lumovie.service;
import com.diph.lumovie.dto.request.CreateMovieRequest;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.enums.MovieType;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface MovieService {
    PageResponse<MovieResponse> getAll(Pageable pageable);
    MovieResponse getById(Long id);
    MovieResponse getBySlug(String slug);
    PageResponse<MovieResponse> search(String keyword, Pageable pageable);
    PageResponse<MovieResponse> getByGenre(Long genreId, Pageable pageable);
    PageResponse<MovieResponse> getByType(MovieType type, Pageable pageable);
    List<MovieResponse> getTrending();
    List<MovieResponse> getTopRated();
    List<MovieResponse> getLatest();
    MovieResponse create(CreateMovieRequest request);
    MovieResponse update(Long id, CreateMovieRequest request);
    void delete(Long id);
    void incrementView(Long id);
}
