package candelasaeatandlodge;
import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

public class ReservationApp {
    public static void main(String[] args) {
       
        String url="jdbc:mysql://localhost:3306/candelasaeatandlodge";
        String username="root";
        String password="";
       
        Scanner scanner = new Scanner(System.in);
        int choice;
  try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM room")) {

            while (resultSet.next()) {
                int room_number = resultSet.getInt("room_number"); // Assuming "id" is the name of the column representing room IDs
                String room_type = resultSet.getString("room_type"); // Assuming "type" is the name of the column representing room types
             
                System.out.println("Room ID: " + room_number + ", Type: " + room_type );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        do {
            System.out.println("Choose an option:");
            System.out.println("1. Room Reservation");
            System.out.println("2. Food Order");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    roomReservation();
                    break;
                case 2:
                    foodOrder();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (choice != 3);
        
        scanner.close();
    }

  public static void roomReservation() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Room Reservation:");
    System.out.println("1. Check Room Availability");
    System.out.println("2. View Room Types");
    System.out.print("Enter your choice: ");
    int choice = 0;
    try {
        choice = scanner.nextInt();
    } catch (InputMismatchException e) {
        System.out.println("Invalid input. Please enter a valid number.");
        scanner.nextLine(); // Consume the invalid input
        roomReservation(); // Restart the method
        return;
    }
    switch (choice) {
        case 1:
            System.out.println("Checking Room Availability...");

            // Add logic for checking room availability
            break;
        case 2:
            System.out.println("Viewing Room Types...");
            // Add logic for viewing room types
            break;
        default:
            System.out.println("Invalid choice.");
            break;
    }
    scanner.close();
}

    public static void foodOrder() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Food Order:");
        System.out.println("1. View Menu");
        System.out.println("2. Place Order");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("Viewing Menu...");
                // Add logic for viewing menu
                break;
            case 2:
                System.out.println("Placing Order...");
                // Add logic for placing order
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
        scanner.close();
    }
}


