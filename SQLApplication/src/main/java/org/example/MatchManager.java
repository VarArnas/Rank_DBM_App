package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchManager {

    Connection dbContext;

    public MatchManager(Connection dbContext) {
        this.dbContext = dbContext;
    }

    public void ShowAllMatches() {
        String sql = "SELECT * FROM rank_sys.matches";
        try (PreparedStatement pstmt = dbContext.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int matchId = rs.getInt("match_id");
                Timestamp date = rs.getTimestamp("date");
                int winningTeam = rs.getInt("winning_team");

                System.out.println("Match ID: " + matchId + ", Date: " + date + ", Winning team: " + winningTeam);
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public void ShowAllMatchesWithPlayers() {
        String sqlMatches = "SELECT * FROM rank_sys.matches ORDER BY match_id";

        try (PreparedStatement pstmtMatches = dbContext.prepareStatement(sqlMatches);
             ResultSet rsMatches = pstmtMatches.executeQuery()) {

            while (rsMatches.next()) {
                int matchId = rsMatches.getInt("match_id");
                Timestamp date = rsMatches.getTimestamp("date");
                int winningTeam = rsMatches.getInt("winning_team");

                System.out.println("Match ID: " + matchId + ", Date: " + date + ", Winning team: " + winningTeam);
                System.out.println("Match participants:");

                String sqlParticipants = "SELECT player_id, player_team FROM rank_sys.match_players WHERE match_id = ?";
                try (PreparedStatement pstmtParticipants = dbContext.prepareStatement(sqlParticipants)) {
                    pstmtParticipants.setInt(1, matchId);
                    try (ResultSet rsParticipants = pstmtParticipants.executeQuery()) {
                        while (rsParticipants.next()) {
                            int playerId = rsParticipants.getInt("player_id");
                            int playerTeam = rsParticipants.getInt("player_team");
                            System.out.println("Player's ID: " + playerId + ", Player's team: " + playerTeam);
                        }
                    }
                }

                System.out.println("");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    private void ChangePlayerInfoAfterMatchInsertion(int playerId, int eloChange, int matchAmountChange) throws SQLException {
        String sql = "UPDATE rank_sys.players SET ELO = ELO + ?, match_amount = match_amount - ? WHERE player_id = ?";
        try(PreparedStatement pstmt = dbContext.prepareStatement(sql)){
            pstmt.setInt(1, eloChange);
            pstmt.setInt(2, matchAmountChange);
            pstmt.setInt(3, playerId);

            int affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0){
                return;
            } else{
                throw new SQLException("\nCouldn't update the player!!!!\n");
            }
        }
    }

    private boolean DidPlayerPlayLessThanTen(int playerId) throws SQLException {
        String sql = "SELECT match_amount FROM rank_sys.players WHERE player_id = ?";
        try(PreparedStatement pstmt = dbContext.prepareStatement(sql)){
            pstmt.setInt(1, playerId);
            try(ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    return rs.getInt("match_amount") - 1 < 5;
                } else {
                    throw new SQLException("No player was found!!!!");
                }
            }
        }
    }

    private Integer FindWinningTeam(int matchId) throws SQLException{
        String sql = "SELECT * FROM rank_sys.matches WHERE match_id = ?";
        try(PreparedStatement pstmt = dbContext.prepareStatement(sql)){
            pstmt.setInt(1, matchId);
            try(ResultSet rs = pstmt.executeQuery()){
                if(!rs.next()){
                    throw new SQLException("\nMatch doesn't exist!!!!\n");
                } else{
                    return rs.getInt("winning_team");
                }
            }
        }
    }

    public List<MatchParticipant> GetMatchPlayers(int matchId) throws SQLException{

        List<MatchParticipant> matchPlayers = new ArrayList<>();
        String sqlMatchPlayers = "SELECT * FROM rank_sys.match_players WHERE match_id = ?";

        try(PreparedStatement pstmt = dbContext.prepareStatement(sqlMatchPlayers)){
            pstmt.setInt(1, matchId);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    MatchParticipant matchPLayer = new MatchParticipant(rs.getInt("player_id"), rs.getInt("player_team"));
                    matchPlayers.add(matchPLayer);
                }
                return matchPlayers;
            }
        }
    }

    public void DeleteMatchAndUpdatePlayer(int matchId) {
        try {
            dbContext.setAutoCommit(false);
            int winningTeam;
            winningTeam = FindWinningTeam(matchId);
            List<MatchParticipant> matchPlayers = GetMatchPlayers(matchId);

            for(MatchParticipant matchPlayer : matchPlayers) {
                //If match deleted player's loss/win bonuses should be reverted, i.e he should get +/- 300/50
                //(300 or 50 elo is decided whether the player played less than 5 matches)
                //If it was a tie means all players got +25, therefore that elo should be subtracted
                int eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? 300 : 50;
                if (winningTeam == 0)
                    eloChange = -25;
                else if (matchPlayer.team == winningTeam)
                    eloChange = eloChange * (-1);
                ChangePlayerInfoAfterMatchInsertion(matchPlayer.playerId, eloChange, 1);
            }

            DeleteMatch(matchId);
            dbContext.commit();

        } catch(SQLException e){
            try {
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                dbContext.rollback();
            } catch (SQLException ex){
                System.out.println("Couldn't rollback");
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
            }
        } finally {
            try {
                dbContext.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }

    public void DeleteMatch(int matchId) throws SQLException{
        String sql = "DELETE FROM rank_sys.matches WHERE match_id = ?";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("The match was deleted!!!");
            } else {
                throw  new SQLException("Couldn't delete the match!!!");
            }
        }
    }

    public void InsertMatchWithTeams(int winningTeam, List<MatchParticipant> playersAndTeams) {

        boolean hasDuplicates = playersAndTeams.stream()
                .map(player -> player.playerId)
                .distinct()
                .count() != playersAndTeams.size();

        if (hasDuplicates) {
            System.out.println("The same player cannot participate twice in one match!!!");
            return;
        }

        try {
            dbContext.setAutoCommit(false);

            String sqlInsertMatch = "INSERT INTO rank_sys.matches (winning_team) VALUES (?) RETURNING match_id";
            int newMatchId;
            try (PreparedStatement pstmt = dbContext.prepareStatement(sqlInsertMatch)) {
                pstmt.setInt(1, winningTeam);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        newMatchId = rs.getInt(1);
                    } else {
                        throw new SQLException("\nCouldn't register the match!!!!\n");
                    }
                }
            }

            String sqlInsertPlayers = "INSERT INTO rank_sys.match_players (match_id, player_id, player_team) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = dbContext.prepareStatement(sqlInsertPlayers)) {
                for (MatchParticipant player : playersAndTeams) {
                    pstmt.setInt(1, newMatchId);
                    pstmt.setInt(2, player.playerId);
                    pstmt.setInt(3, player.team);
                    pstmt.executeUpdate();
                }
            }

            System.out.println("\nMatch with participants was successfully registered!!!!");

            dbContext.commit();
        } catch (SQLException e) {
            try {
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                dbContext.rollback();
            } catch (SQLException ex) {
                System.out.println("Couldn't rollback");
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
            }
        } finally {
            try {
                dbContext.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }

    public Integer ShowMatch(int matchId) {
        String sql = "SELECT * FROM rank_sys.matches WHERE match_id = ?";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
            pstmt.setInt(1, matchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Match doesn't exist!!!");
                    return null;
                } else {
                    System.out.println("Match: ");
                    System.out.println("ID: " + rs.getInt("match_id") + ", Date: " + rs.getTimestamp("date") + ", Winning team: " + rs.getInt("winning_team"));
                    System.out.println("\n");
                    return rs.getInt("match_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            return null;
        }
    }


    public void ChangeMatchWinnerAndUpdateElo(String winningTeam, int matchId) {
        if (winningTeam.isEmpty()) {
            System.out.println("No team was given");
            return;
        }

        try {
            dbContext.setAutoCommit(false);
            int newWinningTeam;
            int oldWinningTeam = FindWinningTeam(matchId);


            String sql = "UPDATE rank_sys.matches SET  winning_team = ? WHERE match_id = ?";

            try {
                newWinningTeam = Integer.parseInt(winningTeam);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect symbol or integer!!!!");
                return;
            }

            if(oldWinningTeam == newWinningTeam){
                throw new SQLException("This team is already selected!!!");
            }

            try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
                pstmt.setInt(1, newWinningTeam);
                pstmt.setInt(2, matchId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows <= 0) {
                    throw new SQLException("Couldn't change the team of the match!!!");
                }
            }

            List<MatchParticipant> matchPlayers = GetMatchPlayers(matchId);

            //Updating player elo based on who won that match update
            for(MatchParticipant matchPlayer : matchPlayers) {
                int eloChange;
                //If tie (0 == tie) than the player should get either +/- 25
                if(newWinningTeam == 0) {
                    if(matchPlayer.team != oldWinningTeam )
                        //If player had lost, but now it's a tie, he should get back his 300 or 50 elo (depending on if he had 5 matches played)
                        // and an extra 25 for a tie
                        eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? 325 : 25;
                    else
                        //If player had won, but now it's a tie, the elo he got from before (either 300/50) should be taken away,
                        // but he still should get 25 elo for the tie
                        eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? -275 : -25;
                } else if (oldWinningTeam == 0) {
                    if(matchPlayer.team != newWinningTeam)
                        //It was a tie, but now after update player has lost,
                        //therefore his tie elo 25 needs to be taken, so he would end up where he started
                        //and the loss elo of 300/50 should be taken as well now that he has lost after update
                        eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? -325 : -75;
                    else
                        //A tie, but after update the player has won
                        //therefore he had already gotten 25 elo, so now he needs to be added 275/25
                        eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? 275 : 25;
                } else if(matchPlayer.team != newWinningTeam) {
                    //Player had won, but now after update he has lost
                    //therefore the won bonus should be subtracted 300/50
                    //and loss bonus should be added 300/50
                    eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? -600 : -100;
                } else {
                    //Player had lost, but now after update won
                    //therefore the won bonus should be added 300/50
                    //and loss bonus should be subtracted
                    eloChange = DidPlayerPlayLessThanTen(matchPlayer.playerId) ? 600 : 100;
                }

                ChangePlayerInfoAfterMatchInsertion(matchPlayer.playerId, eloChange, 0);
            }

            System.out.println("\nMatch result was changed!!!\n");

            dbContext.commit();

        } catch (SQLException e){
            try {
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                dbContext.rollback();
            } catch (SQLException ex) {
                System.out.println("Couldn't rollback");
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
            }
        } finally {
            try {
                dbContext.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }
}
