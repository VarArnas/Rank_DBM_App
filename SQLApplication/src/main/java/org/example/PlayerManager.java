package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PlayerManager {

    Connection dbContext;

    public PlayerManager(Connection dbContext){
        this.dbContext = dbContext;
    }

    public void RegisterNewPlayer(String username, String email, LocalDate dateOfBirth){
        String sql = "INSERT INTO rank_sys.players (username, email, birthday) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setDate(3, java.sql.Date.valueOf(dateOfBirth));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("New player was registered!");
            } else {
                System.out.println("Couldn't register the player.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public void DeletePlayer(String deletionEmail){
        String sql = "DELETE FROM rank_sys.players WHERE email = ?";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
            pstmt.setString(1, deletionEmail);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Player with " + deletionEmail + " was deleted.");
            } else {
                System.out.println("Player with " + deletionEmail + " wasn't found.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public void UpdatePlayer(String newEmail, String newUsername, Integer playerId) {
        StringBuilder sql = new StringBuilder("UPDATE rank_sys.players SET ");
        List<Object> parameters = new ArrayList<>();

        if (!newEmail.isEmpty()) {
            sql.append("email = ?, ");
            parameters.add(newEmail);
        }

        if (!newUsername.isEmpty()) {
            sql.append("username = ?, ");
            parameters.add(newUsername);
        }

        if (parameters.isEmpty()) {
            System.out.println("An empty string was inputted. Try again.");
            return;
        }

        sql = new StringBuilder(sql.substring(0, sql.length() - 2));
        sql.append(" WHERE player_id = ?");
        parameters.add(playerId);

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Change was successful");
            } else {
                System.out.println("Change wasn't successful");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public Integer FindPlayer(String playerEmail) {
        String sql = "SELECT * FROM rank_sys.players WHERE email = ?";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql)) {
            pstmt.setString(1, playerEmail);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Couldn't find the player");
                    return null;
                } else {
                    System.out.println("Player: ");
                    ShowPlayer(rs);
                    return rs.getInt("player_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            return null;
        }
    }

    private void ShowPlayer(ResultSet rs) throws SQLException {
        int id = rs.getInt("player_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        LocalDate dob = rs.getDate("birthday").toLocalDate();
        int elo = rs.getInt("Elo");
        int matchCount = rs.getInt("match_amount");
        int rankId = rs.getInt("rank_id");

        System.out.println("ID: " + id + ", USERNAME: " + username + ", EMAIL: " + email +
                ", BIRTHDAY: " + dob + ", ELO: " + elo + ", MATCH AMOUNT: " + matchCount + ", RankId: " + rankId);
    }

    public void ShowPlayersWithRanks(){
        String sql = "SELECT * FROM rank_sys.players_with_ranks";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()){

            while (rs.next()) {
                int id = rs.getInt("player_id");
                String username = rs.getString("username");
                int elo = rs.getInt("elo");
                int match_amount = rs.getInt("match_amount");
                int rank_id = rs.getInt("rank_id");
                String rankName = rs.getString("rank_name");

                System.out.println("PlayerID: " + id + ", Username: " + username + ", Elo: " + elo
                + ", MatchAmount: " + match_amount + ", RankID: " + rank_id
                + ", RankName: " + rankName);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public void ShowAllPlayers() {
        String sql = "SELECT * FROM rank_sys.players";

        try (PreparedStatement pstmt = dbContext.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ShowPlayer(rs);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }
}
