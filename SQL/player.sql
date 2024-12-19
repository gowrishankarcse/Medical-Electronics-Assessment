
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50)  NOT NULL,
    wins INT DEFAULT 0,
    best_time BIGINT DEFAULT NULL
);
