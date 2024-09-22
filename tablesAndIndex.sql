--tables-----------------

CREATE DATABASE rank_db;

CREATE SCHEMA rank_sys;

CREATE TABLE rank_sys.players (
    player_id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL CHECK (email LIKE '%@%'),
    birthday DATE NOT NULL,
    elo INTEGER NOT NULL DEFAULT 500,
    match_amount INTEGER CHECK (match_amount >= 0) DEFAULT 0,
	rank_id INTEGER REFERENCES rank_sys.ranks(rank_id) ON DELETE SET NULL
);

CREATE TABLE rank_sys.ranks (
	rank_id SERIAL PRIMARY KEY,
	rank_name VARCHAR(255) UNIQUE NOT NULL,
	eloMin INTEGER NOT NULL CHECK(eloMin >= 0),
	eloMax INTEGER NOT NULL CHECK(eloMax > eloMin)
);



CREATE TABLE rank_sys.matches (
	match_id SERIAL PRIMARY KEY,
	date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	winning_team INTEGER CHECK(winning_team IN (2, 1, 0)) NOT NULL
);

CREATE TABLE rank_sys.match_players (
	match_player_id SERIAL PRIMARY KEY,
	match_id INTEGER NOT NULL REFERENCES rank_sys.matches(match_id) ON DELETE CASCADE,
	player_id INTEGER REFERENCES rank_sys.players(player_id) ON DELETE SET NULL,
	player_team INTEGER CHECK(player_team IN (2, 1)) NOT NULL
);

CREATE INDEX id_elo ON rank_sys.players(elo);