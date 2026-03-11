CREATE TABLE IF NOT EXISTS movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    description TEXT,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    backdrop_url VARCHAR(500),
    release_year INT,
    duration INT,
    director VARCHAR(255),
    actors TEXT,
    avg_rating DOUBLE DEFAULT 0.0,
    view_count BIGINT DEFAULT 0,
    country VARCHAR(100),
    language VARCHAR(50),
    slug VARCHAR(255) UNIQUE,
    status ENUM('ONGOING','COMPLETED','UPCOMING') DEFAULT 'COMPLETED',
    type ENUM('MOVIE','SERIES','ANIME','TV_SHOW') DEFAULT 'MOVIE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movies_slug (slug),
    INDEX idx_movies_type (type),
    INDEX idx_movies_view_count (view_count DESC),
    FULLTEXT INDEX ft_movies_search (title, original_title, director, actors)
);
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);
