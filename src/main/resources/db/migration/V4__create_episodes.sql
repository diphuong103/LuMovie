CREATE TABLE IF NOT EXISTS episodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    episode_number INT NOT NULL,
    season_number INT DEFAULT 1,
    title VARCHAR(255),
    video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    duration INT,
    view_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    UNIQUE KEY uq_episode (movie_id, season_number, episode_number)
);
