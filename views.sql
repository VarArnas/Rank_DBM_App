

-- A view for joining the ranks table with the players table to show the actual rank names each player has
CREATE VIEW rank_sys.players_with_ranks AS
SELECT 
	z.player_id,
    z.username, 
    z.elo,
    z.match_amount,
    z.rank_id,
    r.rank_name
FROM 
    rank_sys.players z
LEFT JOIN 
    rank_sys.ranks r ON z.rank_id = r.rank_id;



-- A materialized view which joins the players and ranks table to show how many players have what rank

CREATE MATERIALIZED VIEW rank_sys.player_amount_for_rank AS
SELECT 
    r.rank_name AS Rank,
    COUNT(z.player_id) AS "Player Count"
FROM 
    rank_sys.ranks r
LEFT JOIN 
    rank_sys.players z ON z.rank_id = r.rank_id
GROUP BY 
    r.rank_name, r.EloMin
ORDER BY r.EloMin DESC;

REFRESH MATERIALIZED VIEW player_amount_for_rank;