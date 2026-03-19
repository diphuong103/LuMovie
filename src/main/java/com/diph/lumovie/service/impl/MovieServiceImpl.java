package com.diph.lumovie.service.impl;

import com.diph.lumovie.dto.request.CreateMovieRequest;
import com.diph.lumovie.dto.response.*;
import com.diph.lumovie.entity.Genre;
import com.diph.lumovie.entity.Movie;
import com.diph.lumovie.enums.MovieType;
import com.diph.lumovie.exception.ResourceNotFoundException;
import com.diph.lumovie.mapper.MovieMapper;
import com.diph.lumovie.repository.*;
import com.diph.lumovie.service.MovieService;
import com.diph.lumovie.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service @RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapper movieMapper;

    @Override
    public PageResponse<MovieResponse> getAll(Pageable pageable) {
        Page<Movie> page = movieRepository.findAll(pageable);
        return toPageResponse(page);
    }

    @Override
    public MovieResponse getById(Long id) {
        return movieMapper.toResponse(movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id)));
    }


    @Override
    public MovieResponse getBySlug(String slug) {
        return movieMapper.toResponse(movieRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + slug)));
    }

    @Override
    public PageResponse<MovieResponse> search(String keyword, Pageable pageable) {
        return toPageResponse(movieRepository.findByTitleContainingIgnoreCase(keyword, pageable));
    }

    @Override
    public PageResponse<MovieResponse> getByGenre(Long genreId, Pageable pageable) {
        return toPageResponse(movieRepository.findAll(pageable)); // TODO: filter by genre
    }

    @Override
    public PageResponse<MovieResponse> getByType(MovieType type, Pageable pageable) {
        return toPageResponse(movieRepository.findByType(type, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getTrending() {
        return movieRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(movieMapper::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getTopRated() {
        return movieRepository.findTop10ByOrderByAvgRatingDesc()
                .stream()
                .map(movieMapper::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getLatest() {
        return movieRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(movieMapper::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Override
    public List<MovieResponse> getRelated(Long movieId, int limit) {
        return movieRepository
                .findRelated(movieId, PageRequest.of(0, limit))
                .stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    @Override @Transactional
    public MovieResponse create(CreateMovieRequest request) {
        Movie movie = Movie.builder()
            .title(request.getTitle())
            .originalTitle(request.getOriginalTitle())
            .description(request.getDescription())
            .posterUrl(request.getPosterUrl())
            .trailerUrl(request.getTrailerUrl())
            .backdropUrl(request.getBackdropUrl())
            .releaseYear(request.getReleaseYear())
            .duration(request.getDuration())
            .director(request.getDirector())
            .actors(request.getActors())
            .country(request.getCountry())
            .language(request.getLanguage())
            .status(request.getStatus())
            .type(request.getType())
            .slug(SlugUtils.toSlug(request.getTitle()))
            .build();
        if (request.getGenreIds() != null) {
            movie.setGenres(genreRepository.findAllById(request.getGenreIds()));
        }
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override @Transactional
    public MovieResponse update(Long id, CreateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setStatus(request.getStatus());
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override @Transactional
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) throw new ResourceNotFoundException("Movie not found: " + id);
        movieRepository.deleteById(id);
    }

    @Override @Transactional
    public void incrementView(Long id) { movieRepository.incrementViewCount(id); }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getFeatured() {
        return movieRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .findFirst()
                .map(movieMapper::toResponse)
                .orElse(null);
    }


    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    private PageResponse<MovieResponse> toPageResponse(Page<Movie> page) {
        return PageResponse.<MovieResponse>builder()
            .content(page.getContent().stream().map(movieMapper::toResponse).collect(Collectors.toList()))
            .pageNumber(page.getNumber()).pageSize(page.getSize())
            .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
            .last(page.isLast()).build();
    }
}
