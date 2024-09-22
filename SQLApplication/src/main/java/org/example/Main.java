package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static PlayerManager playerManager;

    static MatchManager matchManager;

    static RankManager rankManager;

    static Connection con;
    static Scanner scanner;
    private static void loadDriver()
    {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't find driver class!");
            cnfe.printStackTrace();
            System.exit(1);
        }
    }
    private static Connection getConnection() {
        Connection postGresConn = null;
        try {
            postGresConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rank_db?ssl=false", "postgres", "admin123") ;
        }
        catch (SQLException sqle) {
            System.out.println("Couldn't connect to database!");
            sqle.printStackTrace();
            return null ;
        }
        System.out.println("Successfully connected to Postgres Database");

        return postGresConn ;
    }

    private static void DisplayChoices() {
        System.out.println("1. Register a new player.");
        System.out.println("2. Delete a player");
        System.out.println("3. Update player.");
        System.out.println("4. Review all players.");
        System.out.println("5. Add a match and its players.");
        System.out.println("6. Delete a match.");
        System.out.println("7. Update a match.");
        System.out.println("8. Review all matches.");
        System.out.println("9. Review all matches with their players.");
        System.out.println("10. Review all players with their ranks.");
        System.out.println("11. Review all existing ranks.");
        System.out.println("12. Review Player count in each rank.");
        System.out.println("13. Exit the program.");
    }

    private static int GetUserChoice() {
        System.out.print("Input the number of the operation you wish to invoke: ");
        return scanner.nextInt();
    }
    public static void HandleChoice(int choice) throws SQLException {
        switch (choice) {
            case 1:
                HandleNewUserRegistration();
                CreateUI();
                break;
            case 2:
                DeleteAnExistingUser();
                CreateUI();
                break;
            case 3:
                UpdateUser();
                CreateUI();
                break;
            case 4:
                ShowAllUsers();
                CreateUI();
                break;
            case 5:
                CreateANewMatch();
                CreateUI();
                break;
            case 6:
                DeleteMatch();
                CreateUI();
                break;
            case 7:
                UpdateMatch();
                CreateUI();
                break;
            case 8:
                DisplayAllMatches();
                CreateUI();
                break;
            case 9:
                DisplayAllMatchesWithTeams();
                CreateUI();
                break;
            case 10:
                DisplayAllPlayersWithTheirRanks();
                CreateUI();
                break;
            case 11:
                DisplayAllRanks();
                CreateUI();
                break;
            case 12:
                DisplayPlayerCount();
                CreateUI();
            case 13:
                ExitProgramAndCloseConnection();
                break;
            default:
                System.out.println("This operation doesn't exist. Try again\n");
                CreateUI();
                break;

        }
    }

    private static void CreateUI() throws SQLException {
        DisplayChoices();
        int choice = GetUserChoice();
        HandleChoice(choice);
    }

    public static void main(String[] args) throws SQLException {
        loadDriver();
        con = getConnection();
        if( null != con ) {
            scanner = new Scanner(System.in);
            playerManager = new PlayerManager(con);
            matchManager = new MatchManager(con);
            rankManager = new RankManager(con);
            CreateUI();
        }
    }

    private static void HandleNewUserRegistration(){
        System.out.print("Input the new player's name: ");
        String username = scanner.next();

        System.out.print("Input the new player's email: ");
        String email = scanner.next();

        System.out.print("Input the birthday (YYYY-MM-DD): ");
        String dob = scanner.next();

        LocalDate dateOfBirth;
        LocalDate currentDate = LocalDate.now();

        try{
            dateOfBirth = LocalDate.parse(dob);
        } catch (DateTimeParseException e){
            System.out.println("\n");
            System.out.println("The inputted date wasn't correct!!!!");
            System.out.println("\n");
            return;
        }

        if (Period.between(dateOfBirth, currentDate).getYears() >= 18) {
            playerManager.RegisterNewPlayer(username, email, dateOfBirth);
        } else {
            System.out.println("\nThe player must be atleast 18 years old!!!. Try again\n");
        }
    }

    private static void DeleteAnExistingUser(){
        System.out.println("Input the email of the player you wish to delete: ");
        String deletionEmail = scanner.next();
        playerManager.DeletePlayer(deletionEmail);
        System.out.println("\n");
    }

    private static void UpdateUser(){
        System.out.println("\nInput the email of the player you wish to update: ");
        String updateEmail = scanner.next();
        Integer playerId = playerManager.FindPlayer(updateEmail);
        if(playerId == null){
            System.out.println("\n");
            return;
        }
        System.out.println("\nInput a new name (if you don't want to change it press enter): ");
        scanner.nextLine();
        String newUsername = scanner.nextLine();
        System.out.println("\nInput a new email (if you don't want to change it press enter): ");
        String newEmail = scanner.nextLine();
        playerManager.UpdatePlayer(newEmail, newUsername, playerId);
    }

    private static void ShowAllUsers(){
        playerManager.ShowAllPlayers();
        System.out.println("\n");
    }

    private static void CreateANewMatch(){
        List<MatchParticipant> playersWithTeams = new ArrayList<>();
        System.out.println("Input the team which won the match (2, 1, or 0 - tie): ");
        int winningTeam = scanner.nextInt();

        System.out.println("\nExisting players:\n");
        playerManager.ShowAllPlayers();
        System.out.println("\n");

        for (int i = 0; i < 4; i++) {
            System.out.println("Input  " + (i + 1) + " player's ID and team (2 or 1): ");
            int playerId = scanner.nextInt();
            int team = scanner.nextInt();
            playersWithTeams.add(new MatchParticipant(playerId, team));
        }
        matchManager.InsertMatchWithTeams(winningTeam, playersWithTeams);
    }

    private static void DeleteMatch(){
        System.out.println("\nExisting matches:\n");
        matchManager.ShowAllMatchesWithPlayers();
        System.out.println("\nInput the match id which you wish to delete: ");
        int deletedMatchId = scanner.nextInt();
        matchManager.DeleteMatchAndUpdatePlayer(deletedMatchId);
    }

    private static void UpdateMatch(){
        System.out.println("\nExisting matches:\n");
        matchManager.ShowAllMatchesWithPlayers();
        System.out.println("\n");
        System.out.println("Input the id of the match which you wish to change: ");
        int updatingMatchId = scanner.nextInt();
        Integer validMatchId = matchManager.ShowMatch(updatingMatchId);
        if(validMatchId == null){
            System.out.println("\n");
            return;
        }
        System.out.println("Input the team which won the match (2, 1, or 0 - tie): ");
        scanner.nextLine();
        String winingTeam = scanner.nextLine();
        matchManager.ChangeMatchWinnerAndUpdateElo(winingTeam, updatingMatchId);
    }

    private static void DisplayAllMatches(){
        matchManager.ShowAllMatches();
        System.out.println("\n");
    }

    private static void DisplayAllMatchesWithTeams(){
        matchManager.ShowAllMatchesWithPlayers();
        System.out.println("\n");
    }

    private static void DisplayAllPlayersWithTheirRanks(){
        System.out.println("\n");
        playerManager.ShowPlayersWithRanks();
        System.out.println("\n");
    }

    private static void DisplayAllRanks(){
        System.out.println("\n");
        rankManager.ShowAllRanks();
        System.out.println("\n");
    }

    private static void DisplayPlayerCount() throws SQLException {
        System.out.println("\n");
        rankManager.PlayerCountInRank();
        System.out.println("\n");
    }

    private static void ExitProgramAndCloseConnection(){
        System.out.println("Exiting the program...");
        if (con != null) {
            try {
                con.close();
            } catch (SQLException exp) {
                System.out.println("Could not disconnect from the database!!!");
                exp.printStackTrace();
            }
        }
        System.exit(0);
    }
}