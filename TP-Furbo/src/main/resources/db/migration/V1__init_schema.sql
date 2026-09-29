CREATE TABLE leagues (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    league_id BIGINT REFERENCES leagues(id)
);

CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(100),
    nationality VARCHAR(100),
    team_id BIGINT REFERENCES teams(id),
    league_id BIGINT REFERENCES leagues(id),
    metrics JSONB
);
