CREATE EXTENSION pg_trgm;

CREATE SCHEMA imdb;

CREATE TABLE imdb.show
(
    imdb_id       VARCHAR(10) PRIMARY KEY      NOT NULL,
    primary_title TEXT                         NOT NULL,
    start_year    CHAR(4),
    end_year      CHAR(4),
    imdb_rating   DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    num_votes     INTEGER          DEFAULT 0   NOT NULL
);

CREATE TABLE imdb.episode
(
    show_id       VARCHAR(10) REFERENCES imdb.show NOT NULL,
    episode_id    VARCHAR(10) PRIMARY KEY          NOT NULL,
    episode_title TEXT,
    season_num    INTEGER                          NOT NULL,
    episode_num   INTEGER                          NOT NULL,
    imdb_rating   DOUBLE PRECISION DEFAULT 0.0     NOT NULL,
    num_votes     INTEGER          DEFAULT 0       NOT NULL
);

CREATE TABLE imdb.thumbnails
(
    imdb_id       VARCHAR(10) PRIMARY KEY NOT NULL,
    thumbnail_url TEXT,
    constraint thumbnails_show_imdb_id_fk
        foreign key (imdb_id) references imdb.show
);

CREATE INDEX episode_show_id_index
    ON imdb.episode (show_id);

CREATE INDEX trigram_index
    ON imdb.show USING GIN (primary_title gin_trgm_ops);

CREATE INDEX ON imdb.show (imdb_rating DESC);