-- Gets called whenever a player gets added to the match_players table, which signals that a player has played a match and either won or lost, therefore his elo needs to be updated
-- updates the player's elo, by adding/subtracting 300/50 elo points (if he has played less than 5 games then 300 elo points will be chosen) if he has won/lost, 
-- if it was a tie, i.e. winning team was entered as 0, then 25 elo points get added

CREATE OR REPLACE FUNCTION adjust_elo_and_match_amount()
RETURNS TRIGGER AS $$
DECLARE
    won_team INTEGER;
    extraElo INTEGER;
BEGIN
    SELECT winning_team INTO won_team FROM rank_sys.matches 
    WHERE match_id = NEW.match_id;

	UPDATE rank_sys.players SET match_amount = match_amount + 1 
    WHERE player_id = NEW.player_id;    
    
    IF NEW.player_id IN (SELECT player_id FROM rank_sys.players WHERE match_amount < 5) THEN
        extraElo := 300;
    ELSE
        extraElo := 50;
    END IF;
    
    IF won_team = 0 THEN
    	UPDATE rank_sys.players SET Elo = Elo + 25 
        WHERE player_id = NEW.player_id;
    ELSEIF NEW.player_team = won_team THEN
        UPDATE rank_sys.players SET Elo = Elo + extraElo 
        WHERE player_id = NEW.player_id;
    ELSEIF (SELECT Elo from rank_sys.players where player_id = NEW.player_id) - extraElo >= 0 THEN
        UPDATE rank_sys.players SET Elo = Elo - extraElo 
        WHERE player_id = NEW.player_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_match_players_insert
AFTER INSERT ON rank_sys.match_players
FOR EACH ROW EXECUTE FUNCTION adjust_elo_and_match_amount();




-- Gets called whenever a player's elo gets updated (most of the time this will be triggered by the after_match_players_insert trigger), to check and maybe update the players rank by changing the FR rank_id
-- it decides if the rank_id FR inside player needs to be changed, based off the elo the player has, and into which rank interval it falls into.

CREATE OR REPLACE FUNCTION adjust_player_rank()
RETURNS TRIGGER AS $$
DECLARE
	neededRank INTEGER;
	currentRank INTEGER;
BEGIN
	SELECT rank_id INTO neededRank FROM rank_sys.ranks 
    WHERE NEW.Elo >= elomin 
    AND (NEW.Elo < EloMax OR EloMax IS NULL);

	SELECT rank_id INTO currentRank FROM rank_sys.players 
    WHERE player_id = NEW.player_id;
	
	IF (currentRank IS NULL OR neededRank <> currentRank) AND NEW.match_amount >= 5 THEN
		UPDATE rank_sys.players SET rank_id = neededRank 
        WHERE player_id = NEW.player_id;
	END IF;
	
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_players_elo_update
AFTER UPDATE OF Elo ON rank_sys.players
FOR EACH ROW EXECUTE FUNCTION adjust_player_rank();




-- Gets called whenever a rank is deleted, to adjust its neighboring ranks elo intervals and also changes players' rank_id FR in the players table
-- if their elo now belongs to a different rank interval 


CREATE OR REPLACE FUNCTION rank_deletion()
RETURNS TRIGGER AS $$
DECLARE
    deletedEloMin INTEGER;
    deletedEloMax INTEGER;
    nextClosestRank INTEGER;
BEGIN
    deletedEloMin := OLD.EloMin;
    deletedEloMax := OLD.EloMax;

    IF deletedEloMin = 0 THEN
        SELECT rank_id INTO nextClosestRank FROM rank_sys.ranks
        WHERE elomin = deletedEloMax;

        UPDATE rank_sys.ranks SET elomin = 0 
        WHERE rank_id = nextClosestRank;
    ELSE
    	SELECT rank_id INTO nextClosestRank FROM rank_sys.ranks
        WHERE EloMax = deletedEloMin;
    
        UPDATE rank_sys.ranks SET EloMax = deletedEloMax 
        WHERE rank_id = nextClosestRank;
    END IF;

    UPDATE rank_sys.players SET rank_id = nextClosestRank
    WHERE rank_id = OLD.rank_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_rank_deletion
BEFORE DELETE ON rank_sys.ranks
FOR EACH ROW EXECUTE FUNCTION rank_deletion();





-- Gets called before inserting a new rank. Firstly checks if the proposed new rank's elo interval doesn't destroy some other rank 
-- (i.e. the new ranks interval completely absorbs some other ranks interval).
-- If no exception gets raised, then determines how the new rank will effect its neighbouring rank intervals 
-- (i.e. if the new rank interval starts or ends at some other ranks interval or if it is inside a ranks interval) and adjusts the intervals based on that.

CREATE OR REPLACE FUNCTION before_insert_into_ranks()
RETURNS TRIGGER AS $$
DECLARE
    tempRankId INTEGER;
    rankSameMax RECORD;
    rankSameMin RECORD;
    rankFullOverlap RECORD;
BEGIN    
    SELECT rank_id INTO tempRankId FROM rank_sys.ranks 
    WHERE NEW.elomin <= elomin 
    AND NEW.elomax >= elomax;

    IF tempRankId IS NOT NULL THEN
    	RAISE EXCEPTION 'the created rank would destroy an existing rank!!!';
    END IF;

    SELECT * INTO rankSameMin FROM rank_sys.ranks 
    WHERE NEW.elomin = elomin;

    SELECT * INTO rankSameMax FROM rank_sys.ranks 
    WHERE NEW.elomax = elomax;

    SELECT * INTO rankFullOverlap FROM rank_sys.ranks
    WHERE NEW.elomax < elomax
    AND NEW.elomin > elomin;

    IF rankSameMin IS NOT NULL THEN
    	UPDATE rank_sys.ranks SET elomin = NEW.elomax 
        WHERE rank_id = rankSameMin.rank_id;
    ELSEIF rankSameMax IS NOT NULL THEN
    	UPDATE rank_sys.ranks SET elomax = NEW.elomin 
        WHERE rank_id = rankSameMax.rank_id;
    ELSEIF rankFullOverlap IS NOT NULL THEN
        UPDATE rank_sys.ranks SET elomin = NEW.elomax
        WHERE elomin = rankFullOverlap.elomax;

        UPDATE rank_sys.ranks SET elomax = NEW.elomin
        WHERE rank_id = rankFullOverlap.rank_id;
    ELSE
    	SELECT * INTO rankSameMax FROM rank_sys.ranks 
        WHERE elomin < NEW.elomin 
        ORDER BY elomin DESC 
        LIMIT 1;

    	UPDATE rank_sys.ranks SET elomax = NEW.elomin
        WHERE rank_id = rankSameMax.rank_id;

    	SELECT * INTO rankSameMin FROM rank_sys.ranks 
        WHERE elomax > NEW.elomax 
        ORDER BY elomax ASC 
        LIMIT 1;

    	UPDATE rank_sys.ranks SET elomin = NEW.elomax 
        WHERE rank_id = rankSameMin.rank_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER before_rank_insertion
BEFORE INSERT ON rank_sys.ranks
FOR EACH ROW EXECUTE FUNCTION before_insert_into_ranks();





-- Gets called after insertion of a new rank, and adjusts the players ranks by changing the rank_id FR inside the players table, if their elo now belongs to a different ranks elo interval

CREATE OR REPLACE FUNCTION after_insert_into_ranks()
RETURNS TRIGGER AS $$
DECLARE
    newRankId INTEGER;
    player RECORD;
BEGIN
    FOR player IN SELECT player_id, Elo, match_amount, rank_id FROM rank_sys.players LOOP
        SELECT rank_id INTO newRankId FROM rank_sys.ranks
        WHERE player.Elo >= EloMin 
        AND player.match_amount >= 5 
        ORDER BY elomin DESC 
        LIMIT 1;

        IF (newRankId IS NOT NULL AND newRankId <> player.rank_id) THEN
            UPDATE rank_sys.players SET rank_id = newRankId
            WHERE player_id = player.player_id;
        END IF;
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_rank_insertion
AFTER INSERT ON rank_sys.ranks
FOR EACH ROW EXECUTE FUNCTION after_insert_into_ranks();





--Gets called before insertion of a match_player  and checks if a match has less than 4 players, if that match team has less than 2 players and if there is no one player pariticipating twice in the same match

CREATE OR REPLACE FUNCTION check_match_id_insertion()
RETURNS TRIGGER AS $$
DECLARE
    matchPlayerAmount INTEGER;
    teamPlayerAmount INTEGER;
    playerId INTEGER;
BEGIN
    SELECT COUNT(*) INTO matchPlayerAmount FROM rank_sys.match_players
    WHERE match_id = NEW.match_id;

    IF matchPlayerAmount >= 4 THEN
        RAISE EXCEPTION 'Match already has 4 players!!!';
    END IF;

    SELECT player_id INTO playerId FROM rank_sys.match_players
    WHERE match_id = NEW.match_id;

    IF playerId = NEW.player_id THEN
        RAISE EXCEPTION 'This player already participated in this match!!!';
    END IF;

    SELECT COUNT(*) INTO teamPlayerAmount FROM rank_sys.match_players
    WHERE match_id = NEW.match_id 
    AND player_team = NEW.player_team;

    IF teamPlayerAmount >= 2 THEN
        RAISE EXCEPTION 'This team already has 2 players!!!';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_match_players
BEFORE INSERT ON rank_sys.match_players
FOR EACH ROW EXECUTE FUNCTION check_match_id_insertion();


-- For data integrity when discussing elo changes of players, whenever a match is deleted or the winning team is updated, 
-- it is handled inside the business logic of a the application responsible for managing this database

