CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    icon VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO genres (name, slug, icon) VALUES
('Hành Động','hanh-dong','💥'),('Tình Cảm','tinh-cam','❤️'),
('Hài Hước','hai-huoc','😂'),('Kinh Dị','kinh-di','👻'),
('Hoạt Hình','hoat-hinh','🎨'),('Khoa Học Viễn Tưởng','khoa-hoc-vien-tuong','🚀'),
('Phiêu Lưu','phieu-luu','🗺️'),('Tâm Lý','tam-ly','🧠');
