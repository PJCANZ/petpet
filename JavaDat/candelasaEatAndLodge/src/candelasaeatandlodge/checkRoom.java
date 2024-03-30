package candelasaeatandlodge;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class checkRoom {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/candelasaeatandlodge";
        String username = "root";
        String password = ""; // Make sure to provide your MySQL password here

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM room")) {

            while (resultSet.next()) {
                int room_number = resultSet.getInt("room_number"); // Assuming "id" is the name of the column representing room IDs
                String room_type = resultSet.getString("room_type"); // Assuming "type" is the name of the column representing room types
                int rate = resultSet.getInt("rate");
                int availability = resultSet.getInt("availabity");
                
                System.out.println("Room ID: " + room_number + ", Type: " + room_type  + ", Room Rate " + rate + ", Availability " + availability);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
