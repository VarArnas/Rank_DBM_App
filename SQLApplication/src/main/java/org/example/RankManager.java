package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankManager {
    Connection dbContext;
    public RankManager(Connection dbContext){
        this.dbContext = dbContext;
    }

    public void ShowAllRanks(){
        String sql = "SELECT * FROM rank_sys.ranks";

        try(PreparedStatement pstmt = dbContext.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){

            while (rs.next()){
              int rankId = rs.getInt("rank_id");
              String rankName = rs.getString("rank_name");
              int eloMin = rs.getInt("EloMin");
              int eloMax = rs.getInt("EloMax");
              System.out.println("Rank ID: " + rankId + ", Rank name: " + rankName +
                      ", Min elo: " + eloMin + ", Max elo: " + eloMax);
            }

        } catch (SQLException e){
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }

    public void RefreshMatView() throws SQLException {
        String sql = "REFRESH MATERIALIZED VIEW rank_sys.player_amount_for_rank";
        PreparedStatement pstmt = null;
        try{
            pstmt = dbContext.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e){
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        } finally {
            if(pstmt != null) pstmt.close();
        }
    }

    public void PlayerCountInRank() throws SQLException {
        try {
            dbContext.setAutoCommit(false);

            RefreshMatView();

            String sql = "SELECT * FROM rank_sys.player_amount_for_rank";
            try (PreparedStatement pstmt = dbContext.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String rankName = rs.getString("Rank");
                    int playerCount = rs.getInt("Player Count");

                    System.out.println("RankName: " + rankName + ", PlayerCount: " + playerCount);
                }
            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
            }

            dbContext.commit();

        } catch (SQLException e) {
            dbContext.rollback();
            throw e;
        } finally {
            dbContext.setAutoCommit(true);
        }
    }
}
