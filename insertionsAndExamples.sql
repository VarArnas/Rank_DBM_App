--inserting players

INSERT INTO rank_sys.players (username, email, birthday) VALUES
('GamerOne', 'gamerone@example.com', '2000-01-01'),
('DragonSlayer', 'dragonslayer@example.com', '2000-01-02'),
('MysticWarrior', 'mysticwarrior@example.com', '2000-01-03'),
('ShadowHunter', 'shadowhunter@example.com', '2000-01-04'),
('EagleEye', 'eagleeye@example.com', '2000-01-05'),
('NinjaRogue', 'ninjarogue@example.com', '2000-01-06'),
('FrostWizard', 'frostwizard@example.com', '2000-01-07'),
('IronKnight', 'ironknight@example.com', '2000-01-08'),
('PhoenixRider', 'phoenixrider@example.com', '2000-01-09'),
('StealthAssassin', 'stealthassassin@example.com', '2000-01-10'),
('ThunderMage', 'thundermage@example.com', '2000-01-11'),
('CrystalSorcerer', 'crystalsorcerer@example.com', '2000-01-12'),
('BladeMaster', 'blademaster@example.com', '2000-01-13'),
('AbyssWalker', 'abysswalker@example.com', '2000-01-14'),
('SkyPirate', 'skypirate@example.com', '2000-01-15'),
('VoidSeeker', 'voidseeker@example.com', '2000-01-16'),
('FlameGuardian', 'flameguardian@example.com', '2000-01-17'),
('DuskRanger', 'duskranger@example.com', '2000-01-18'),
('StormBreaker', 'stormbreaker@example.com', '2000-01-19'),
('LightningFury', 'lightningfury@example.com', '2000-01-20');


--inserting ranks

INSERT INTO rank_sys.ranks (rank_name, EloMin, EloMax) VALUES
('BEGINNER', 0, 300),
('SEMI-BEGINNER', 300, 500),
('NOVICE', 500, 1000),
('INTERMEDIATE', 1000, 1350),
('ADVANCED', 1350, 1800),
('MASTER', 1800, 2000),
('LEGEND', 2000, 10000);
	

--inserting matches
	
INSERT INTO rank_sys.matches (winning_team) VALUES
(1),
(1),
(1),  
(1),
(2), 
(0),  
(1), 
(2),  
(1),  
(0), 
(1), 
(0), 
(2), 
(2), 
(1), 
(0), 
(2), 
(1), 
(2), 
(0), 
(1), 
(2), 
(1), 
(0);


--inserting match players

INSERT INTO rank_sys.match_players (match_id, player_id, player_team) VALUES
(1, 1, 1), (1, 2, 1), (1, 3, 2), (1, 4, 2),
(2, 1, 2), (2, 2, 2), (2, 3, 1), (2, 4, 1),
(3, 1, 1), (3, 2, 1), (3, 3, 2), (3, 4, 2),
(4, 1, 2), (4, 2, 2), (4, 3, 1), (4, 4, 1),
(5, 1, 1), (5, 2, 1), (5, 3, 2), (5, 4, 2),
(6, 1, 1), (6, 5, 1), (6, 6, 2), (6, 4, 2),
(7, 7, 1), (7, 10, 1), (7, 9, 2), (7, 4, 2),
(8, 7, 1), (8, 10, 1), (8, 6, 2), (8, 3, 2),
(9, 6, 1), (9, 10, 1), (9, 16, 2), (9, 17, 2),
(10, 5, 1), (10, 4, 1), (10, 9, 2), (10, 10, 2),
(11, 6, 1), (11, 1, 1), (11, 2, 2), (11, 11, 2),
(12, 6, 1), (12, 8, 1), (12, 18, 2), (12, 16, 2),
(13, 7, 1), (13, 13, 1), (13, 3, 2), (13, 16, 2),
(14, 8, 1), (14, 9, 1), (14, 10, 2), (14, 11, 2),
(15, 8, 1), (15, 9, 1), (15, 10, 2), (15, 11, 2),
(16, 8, 1), (16, 9, 1), (16, 10, 2), (16, 11, 2),
(17, 4, 1), (17, 3, 1), (17, 2, 2), (17, 1, 2),
(18, 5, 1), (18, 6, 1), (18, 7, 2), (18, 8, 2),
(19, 12, 1), (19, 11, 1), (19, 10, 2), (19, 9, 2),
(20, 13, 1), (20, 14, 1), (20, 15, 2), (20, 16, 2),
(21, 20, 1), (21, 19, 1), (21, 18, 2), (21, 17, 2),
(22, 20, 1), (22, 18, 1), (22, 6, 2), (22, 3, 2),
(23, 20, 1), (23, 18, 1), (23, 10, 2), (23, 9, 2),
(24, 20, 1), (24, 19, 1), (24, 14, 2), (24, 13, 2);

--refreshing materialized view

REFRESH MATERIALIZED VIEW zaideju_skaicius_ranguose;




