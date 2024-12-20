import java.sql.*;
import java.util.*;

class Player {
    String name;
    int wins;
    long bestTime;

    public Player(String name, int wins, long bestTime) {
        this.name = name;
        this.wins = wins;
        this.bestTime = bestTime;
    }
}

public class Game {
    private static final String DB_URL = "jdbc:mysql://localhost:8081/RPS";
    private static final String DB_USER = "gowri";
    private static final String DB_PASSWORD = "pgs2212";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Welcome to Rock, Paper, Scissors!");
            System.out.print("Enter your name: ");
            String playerName = scanner.nextLine();

            Player currentPlayer = findOrCreatePlayer(connection, playerName);

            System.out.println("\nLet's start the game!");
            System.out.println("Choose one: [1] Rock,  [2] Paper, [3] Scissors");
            System.out.print("Your choice: ");
            int playerChoice = scanner.nextInt();

            if (playerChoice < 1 || playerChoice > 3) {
                System.out.println("Invalid choice. Please restart the game.");
                return;
            }

            long startTime = System.currentTimeMillis();

            String playerMove = moveToString(playerChoice);
            String computerMove = moveToString(new Random().nextInt(3) + 1);

            System.out.println("You chose: " + playerMove);
            System.out.println("Computer chose: " + computerMove);

            String result = determineWinner(playerMove, computerMove);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            System.out.println(result);
            System.out.println("Time taken: " + timeTaken + " sec");

            if ("You Win!".equals(result)) {
                currentPlayer.wins++;
                currentPlayer.bestTime = Math.min(currentPlayer.bestTime == 0 ? Long.MAX_VALUE : currentPlayer.bestTime, timeTaken);
                updatePlayerStats(connection, currentPlayer, timeTaken);
            }

            displayLeaderboard(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Player findOrCreatePlayer(Connection connection, String name) throws SQLException {
        String selectQuery = "SELECT name, wins, best_time FROM players WHERE name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, name);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                return new Player(rs.getString("name"), rs.getInt("wins"), rs.getLong("best_time"));
            }
        }

        String insertQuery = "INSERT INTO players (name) VALUES (?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, name);
            insertStmt.executeUpdate();
        }

        return new Player(name, 0, 0);
    }

    private static void updatePlayerStats(Connection connection, Player player, long timeTaken) throws SQLException {
        String updateQuery = "UPDATE players SET wins = ?, best_time = ? WHERE name = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, player.wins);
            updateStmt.setLong(2, player.bestTime);
            updateStmt.setString(3, player.name);
            updateStmt.executeUpdate();
        }
    }

    private static void displayLeaderboard(Connection connection) throws SQLException {
        String leaderboardQuery = "SELECT name, wins, best_time FROM players ORDER BY wins DESC, best_time ASC";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(leaderboardQuery);

            System.out.println("--- Leaderboard ---");
            System.out.printf("%-20s %-10s %-10s\n", "Player Name", "Wins", "Best Time (ms)");
            while (rs.next()) {
                System.out.printf("%-20s %-10d %-10s\n",
                        rs.getString("name"),
                        rs.getInt("wins"),
                        rs.getLong("best_time") == 0 ? "N/A" : rs.getLong("best_time"));
            }
        }
    }

    private static String moveToString(int choice) {
        return switch (choice) {
           case 1:
            return "Rock";
        case 2:
            return "Paper";
        case 3:
            return "Scissors";
        default:
            return "Invalid";

        };
    }

    private static String determineWinner(String playerMove, String computerMove) {
        if (playerMove.equals(computerMove)) {
            return "It's a Draw!";
        }
        if ((playerMove.equals("Rock") && computerMove.equals("Scissors")) ||
            (playerMove.equals("Scissors") && computerMove.equals("Paper")) ||
            (playerMove.equals("Paper") && computerMove.equals("Rock"))) {
            return "You Win!";
        }
        return "Computer Wins!";
    }
}
