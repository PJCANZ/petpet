package receiptionist;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Receiptionist extends JFrame implements ActionListener {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/lodgeandeat";
    static final String USER = "root";
    static final String PASS = "";

    private JButton orderFoodButton, reserveRoomButton, exitButton, logOutTableButton, logOutRoomButton;
    private Connection conn;
    private String customerName;
    private JButton exportButton;
    private JButton exportRoomButton;
    private JButton showFoodDetailsButton;
    private JButton displayTablesButton;
    private JButton displayRoomsButton;
  
    public Receiptionist() {
        setTitle("Stille Liv Lodge and Eat");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Connect to database
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            createDatabaseTables(); // Ensure tables exist
            insertSampleData(); // Insert sample data
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 1)); // Increased grid layout to accommodate new button
        
        // Set the insets to remove excess space (e.g., 10 pixels padding)
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Set the background color of the panel
        mainPanel.setBackground(Color.WHITE); // Change Color.WHITE to your desired color
        
        // Add your existing code for other components and functionality here...
        

        orderFoodButton = new JButton("Order Food");
        orderFoodButton.addActionListener(this);
        mainPanel.add(orderFoodButton);

        reserveRoomButton = new JButton("Reserve Room");
        reserveRoomButton.addActionListener(this);
        mainPanel.add(reserveRoomButton);

        showFoodDetailsButton = new JButton("Show Food Details");
        showFoodDetailsButton.addActionListener(this);
        mainPanel.add(showFoodDetailsButton);

        logOutTableButton = new JButton("Log Out Table");
        logOutTableButton.addActionListener(this);
        mainPanel.add(logOutTableButton);

        logOutRoomButton = new JButton("Log Out Room");
        logOutRoomButton.addActionListener(this);
        mainPanel.add(logOutRoomButton);

        exportButton = new JButton("Display Full Table Data");
        exportButton.addActionListener(this);
        mainPanel.add(exportButton);

        exportRoomButton = new JButton("Display Full Room Data");
        exportRoomButton.addActionListener(this);
        mainPanel.add(exportRoomButton);

        
      
        displayTablesButton = new JButton("Display Tables Data");
        displayTablesButton.addActionListener(this);
        mainPanel.add(displayTablesButton);
        
        displayRoomsButton = new JButton("Display Rooms Data");
        displayRoomsButton.addActionListener(this);
        mainPanel.add(displayRoomsButton);
   
        

        exitButton = new JButton("Exit");
        exitButton.addActionListener(this);
        mainPanel.add(exitButton);

        add(mainPanel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == orderFoodButton) {
            customerName = JOptionPane.showInputDialog("Enter your name:");
            if (customerName != null && !customerName.isEmpty()) {
                int foodChoice = displayFoodMenuAndGetChoice();
                if (foodChoice != 0) {
                    displayAvailableTables(); // Check available tables
                    String tableChoiceStr = JOptionPane.showInputDialog("Enter the number of the table you want to choose:");
                    if (isValidInput(tableChoiceStr)) {
                        int tableChoice = Integer.parseInt(tableChoiceStr);
                        if (isTableAvailable(tableChoice)) {
                            chooseTable(tableChoice);
                            insertOrderIntoDatabase(foodChoice, tableChoice, customerName);
                        } else {
                            JOptionPane.showMessageDialog(this, "Table is not available.");
                        }
                    }
                }
            }
        } else if (e.getSource() == reserveRoomButton) {
            customerName = JOptionPane.showInputDialog("Enter your name:");
            if (customerName != null && !customerName.isEmpty()) {
                displayAvailableRooms(); // Check available rooms
                String roomChoiceStr = JOptionPane.showInputDialog("Enter the number of the room you want to reserve:");
                if (isValidInput(roomChoiceStr)) {
                    int roomChoice = Integer.parseInt(roomChoiceStr);
                    if (isRoomAvailable(roomChoice)) {
                        reserveRoom(roomChoice);
                        insertRoomReservationIntoDatabase(roomChoice, customerName);
                    } else {
                        JOptionPane.showMessageDialog(this, "Room is not available.");
                    }
                }
            }
        } else if (e.getSource() == logOutTableButton) {
            String tableChoiceStr = JOptionPane.showInputDialog("Enter the number of the table you want to log out:");
            if (isValidInput(tableChoiceStr)) {
                int tableChoice = Integer.parseInt(tableChoiceStr);
                logOutTable(tableChoice);
            }
        } else if (e.getSource() == logOutRoomButton) {
            String roomChoiceStr = JOptionPane.showInputDialog("Enter the number of the room you want to log out:");
            if (isValidInput(roomChoiceStr)) {
                int roomChoice = Integer.parseInt(roomChoiceStr);
                logOutRoom(roomChoice);
            }
        } else if (e.getSource() == exportButton) {
            exportDataToFile();
        } else if (e.getSource() == exportRoomButton) {
            exportRoomDataToFile();
        } else if (e.getSource() == showFoodDetailsButton) {
            showFoodDetails();
      
        } else if (e.getSource() == displayTablesButton) {
            displayTablesData();
        } else if (e.getSource() == displayRoomsButton) {
            displayRoomsData();
     
        } else if (e.getSource() == exitButton) {
            dispose();
       
        }
        
    }

    public int displayFoodMenuAndGetChoice() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Food");

            List<String> foodNames = new ArrayList<>();
            while (rs.next()) {
                foodNames.add(rs.getString("Name"));
            }

            rs.close();
            stmt.close();

            // Convert list of food names to array for use in JOptionPane
            Object[] options = foodNames.toArray();

            // Display food options
            int choice = JOptionPane.showOptionDialog(null, "Choose a food item", "Food Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            // Increment choice by 1 to match with FoodID
            return choice + 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch food menu.");
            return 0; // Return 0 indicating failure
        }
    }

    public void displayAvailableTables() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Tables");

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Table Number", "Capacity", "Availability"}, 0);
            while (rs.next()) {
                String availability = rs.getBoolean("IsOccupied") ? "Unavailable" : "Available";
                model.addRow(new Object[]{rs.getInt("TableNumber"), rs.getInt("Capacity"), availability});
            }
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(null, scrollPane, "Available Tables", JOptionPane.PLAIN_MESSAGE);

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch available tables.");
        }
    }

    public void displayAvailableRooms() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms");

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Room Number", "Capacity", "Availability"}, 0);
            while (rs.next()) {
                String availability = rs.getBoolean("IsOccupied") ? "Unavailable" : "Available";
                model.addRow(new Object[]{rs.getInt("RoomNumber"), rs.getInt("Capacity"), availability});
            }
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(null, scrollPane, "Available Rooms", JOptionPane.PLAIN_MESSAGE);

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch available rooms.");
        }
    }

    public void chooseTable(int choice) {
        // Logic for choosing a table
        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Tables SET IsOccupied = true WHERE TableNumber = ?");
            pstmt.setInt(1, choice);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void reserveRoom(int choice) {
        // Logic for reserving a room
        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Rooms SET IsOccupied = true WHERE RoomNumber = ?");
            pstmt.setInt(1, choice);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void logOutTable(int tableID) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Tables SET IsOccupied = false WHERE TableID = ?");
            pstmt.setInt(1, tableID);
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Table with TableID " + tableID + " has been logged out.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to log out the table with TableID " + tableID);
        }
    }
    

    public void logOutRoom(int choice) {
        // Logic for logging out a room
        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Rooms SET IsOccupied = false WHERE RoomNumber = ?");
            pstmt.setInt(1, choice);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isTableAvailable(int tableNumber) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT IsOccupied FROM Tables WHERE TableNumber = ?");
            pstmt.setInt(1, tableNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isOccupied = rs.getBoolean("IsOccupied");
                rs.close();
                pstmt.close();
                return !isOccupied;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isRoomAvailable(int roomNumber) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT IsOccupied FROM Rooms WHERE RoomNumber = ?");
            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isOccupied = rs.getBoolean("IsOccupied");
                rs.close();
                pstmt.close();
                return !isOccupied;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void insertOrderIntoDatabase(int foodChoice, int tableChoice, String customerName) {
        try {
            String insertOrderSQL = "INSERT INTO Orders (CustomerName, FoodID, TableID) VALUES (?, ?, ?)";
            PreparedStatement insertOrderStatement = conn.prepareStatement(insertOrderSQL);
            insertOrderStatement.setString(1, customerName);
            insertOrderStatement.setInt(2, foodChoice);
            insertOrderStatement.setInt(3, tableChoice);
            insertOrderStatement.executeUpdate();
            insertOrderStatement.close();
            JOptionPane.showMessageDialog(this, "Order placed successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to place order.");
        }
    }

    public void insertRoomReservationIntoDatabase(int roomChoice, String reservationName) {
        try {
            String insertReservationSQL = "INSERT INTO Reservations (ReservationName, RoomID, CheckInDate, CheckOutDate) VALUES (?, ?, ?, ?)";
            PreparedStatement insertReservationStatement = conn.prepareStatement(insertReservationSQL);
            insertReservationStatement.setString(1, reservationName);
            insertReservationStatement.setInt(2, roomChoice);
            // You need to provide check-in and check-out dates. For simplicity, let's assume they are fixed.
            insertReservationStatement.setDate(3, java.sql.Date.valueOf("2024-03-10")); // Example check-in date
            insertReservationStatement.setDate(4, java.sql.Date.valueOf("2024-03-12")); // Example check-out date
            insertReservationStatement.executeUpdate();
            insertReservationStatement.close();
            JOptionPane.showMessageDialog(this, "Room reservation made successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to make room reservation.");
        }
    }
    
    public boolean isValidInput(String input) {
        return input != null && !input.isEmpty();
    }
    private void createDatabaseTables() {
        try {
            Statement stmt = conn.createStatement();
            // Create the database if it does not exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `lodgeandeat`");
            // Switch to the database
            stmt.executeUpdate("USE `lodgeandeat`");
    
            // Create Tables table
            String createTablesTableSQL = "CREATE TABLE IF NOT EXISTS Tables ("
                    + "TableID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "TableNumber INT NOT NULL,"
                    + "Capacity INT NOT NULL,"
                    + "IsOccupied BOOLEAN NOT NULL"
                    + ")";
            stmt.executeUpdate(createTablesTableSQL);
    
            // Create Rooms table
            String createRoomsTableSQL = "CREATE TABLE IF NOT EXISTS Rooms ("
                    + "RoomID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "RoomNumber INT NOT NULL,"
                    + "Capacity INT NOT NULL,"
                    + "IsOccupied BOOLEAN NOT NULL"
                    + ")";
            stmt.executeUpdate(createRoomsTableSQL);
    
            // Create Orders table
            String createOrdersTableSQL = "CREATE TABLE IF NOT EXISTS Orders ("
                    + "OrderID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "TableID INT NOT NULL,"
                    + "FoodID INT NOT NULL,"
                    + "CustomerName VARCHAR(255) NOT NULL"
                    + ")";
            stmt.executeUpdate(createOrdersTableSQL);
    
            // Create Reservations table
            String createReservationsTableSQL = "CREATE TABLE IF NOT EXISTS Reservations ("
                    + "ReservationID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "ReservationName VARCHAR(255) NOT NULL,"
                    + "RoomID INT NOT NULL,"
                  
                    + "CheckInDate DATE NOT NULL,"
                    + "CheckOutDate DATE NOT NULL"
                    + ")";
            stmt.executeUpdate(createReservationsTableSQL);
    
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    private void insertSampleData() {
        try {
            Statement stmt = conn.createStatement();

            // Check if there are any existing tables
            ResultSet tablesResultSet = stmt.executeQuery("SELECT COUNT(*) AS count FROM Tables");
            tablesResultSet.next();
            int tableCount = tablesResultSet.getInt("count");
            tablesResultSet.close();

            // If no tables exist, insert sample tables
            if (tableCount == 0) {
                String insertTablesSQL = "INSERT INTO Tables (TableNumber, Capacity, IsOccupied) VALUES ";
                for (int i = 1; i <= 10; i++) {
                    insertTablesSQL += "(" + i + ", 4, false)";
                    if (i < 10) {
                        insertTablesSQL += ",";
                    }
                }
                stmt.executeUpdate(insertTablesSQL);
            }

            // Check if there are any existing rooms
            ResultSet roomsResultSet = stmt.executeQuery("SELECT COUNT(*) AS count FROM Rooms");
            roomsResultSet.next();
            int roomCount = roomsResultSet.getInt("count");
            roomsResultSet.close();

            // If no rooms exist, insert sample rooms
            if (roomCount == 0) {
                String insertRoomsSQL = "INSERT INTO Rooms (RoomNumber, Capacity, IsOccupied) VALUES ";
                for (int i = 1; i <= 10; i++) {
                    insertRoomsSQL += "(" + (100 + i) + ", 4, false)";
                    if (i < 10) {
                        insertRoomsSQL += ",";
                    }
                }
                stmt.executeUpdate(insertRoomsSQL);
            }

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("SHOW TABLES LIKE 'Food'");
            boolean tableExists = resultSet.next();
            resultSet.close();

            // If the Food table doesn't exist, create it and insert sample data
            if (!tableExists) {
                String createFoodTableSQL = "CREATE TABLE Food ("
                        + "FoodID INT AUTO_INCREMENT PRIMARY KEY,"
                        + "Name VARCHAR(255) NOT NULL,"
                        + "Price DOUBLE NOT NULL,"
                        + "Description TEXT"
                        + ")";
                stmt.executeUpdate(createFoodTableSQL);

                String insertFoodSQL = "INSERT INTO Food (Name, Price, Description) VALUES "
                        + "('Burger', 8.99, 'A classic beef burger with lettuce, tomato, and cheese.'),"
                        + "('Pizza', 10.99, 'A delicious pizza topped with cheese, pepperoni, and bell peppers.'),"
                        + "('Salad', 6.99, 'Fresh mixed greens with cherry tomatoes, cucumbers, and balsamic vinaigrette.'),"
                        + "('Sandwich', 7.99, 'A hearty sandwich with turkey, bacon, lettuce, and mayo.'),"
                        + "('Pasta', 12.99, 'Spaghetti pasta with marinara sauce and meatballs.'),"
                        + "('Steak', 19.99, 'Juicy sirloin steak cooked to perfection, served with mashed potatoes and vegetables.'),"
                        + "('Sushi', 15.99, 'Assorted sushi rolls including tuna, salmon, and California rolls.'),"
                        + "('Chicken Wings', 9.99, 'Crispy chicken wings tossed in buffalo sauce, served with ranch dressing.'),"
                        + "('Tacos', 8.99, 'Soft corn tortillas filled with seasoned ground beef, lettuce, and cheese.'),"
                        + "('Fish and Chips', 11.99, 'Beer-battered fish fillets served with french fries and tartar sauce.')";

                stmt.executeUpdate(insertFoodSQL);

                JOptionPane.showMessageDialog(this, "Food table created and sample data inserted successfully.");
            }

            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create Food table and insert sample data.");
        }
    }
    
    private void exportDataToFile() {
        try {
            // Create a statement to execute SQL queries
            Statement stmt = conn.createStatement();
            
            // Execute the SQL query to retrieve data from the database
            ResultSet tableResultSet = stmt.executeQuery("SELECT tables.TableNumber, tables.Capacity, orders.CustomerName, food.FoodID, food.Name AS FoodName, food.Price " +
                                                         "FROM Tables tables " +
                                                         "LEFT JOIN Orders orders ON tables.TableID = orders.TableID " +
                                                         "LEFT JOIN Food food ON orders.FoodID = food.FoodID");
            
            // Create a DefaultTableModel to hold the data
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Table Number");
            model.addColumn("Capacity");
            model.addColumn("Customer Name");
            model.addColumn("Food ID");
            model.addColumn("Food Name");
            model.addColumn("Food Price");
            
            // Iterate through the result set and add data to the table model
            while (tableResultSet.next()) {
                int tableNumber = tableResultSet.getInt("TableNumber");
                int capacity = tableResultSet.getInt("Capacity");
                String customerName = tableResultSet.getString("CustomerName");
                int foodID = tableResultSet.getInt("FoodID");
                String foodName = tableResultSet.getString("FoodName");
                double foodPrice = tableResultSet.getDouble("Price");
                
                // Check if any value is null, skip the row if any value is null
                if (customerName != null && foodName != null) {
                    // Add row to the table model
                    model.addRow(new Object[]{tableNumber, capacity, customerName, foodID, foodName, foodPrice});
                }
            }
            
            // Close the ResultSet and Statement
            tableResultSet.close();
            stmt.close();
            
            // Create a JTable with the table model
            JTable table = new JTable(model);
            
            // Add the table to a JScrollPane for scrollable display
            JScrollPane scrollPane = new JScrollPane(table);
            
            // Display the table in a JOptionPane
            JOptionPane.showMessageDialog(this, scrollPane, "Tables Data", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            // Print stack trace and show an error message to the user if an exception occurs
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch tables data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
         
private void exportRoomDataToFile() {
    try {
        // Create a statement to execute SQL queries
        Statement stmt = conn.createStatement();
        
        // Execute the SQL query to retrieve data from the database
        ResultSet roomResultSet = stmt.executeQuery("SELECT ReservationID, ReservationName, RoomID, CheckInDate, CheckOutDate FROM Reservations");
        
        // Create a DefaultTableModel to hold the data
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Reservation ID");
        model.addColumn("Reservation Name");
        model.addColumn("Room ID");
        model.addColumn("Check-In Date");
        model.addColumn("Check-Out Date");
        
        // Iterate through the result set and add data to the table model
        while (roomResultSet.next()) {
            int reservationID = roomResultSet.getInt("ReservationID");
            String reservationName = roomResultSet.getString("ReservationName");
            Integer roomID = (Integer) roomResultSet.getObject("RoomID");
            Date checkInDate = roomResultSet.getDate("CheckInDate");
            Date checkOutDate = roomResultSet.getDate("CheckOutDate");
            
            // Add row to the table model
            model.addRow(new Object[]{reservationID, reservationName, roomID == null ? "null" : roomID, checkInDate, checkOutDate});
        }
        
        // Close the ResultSet and Statement
        roomResultSet.close();
        stmt.close();
        
        // Create a JTable with the table model
        JTable table = new JTable(model);
        
        // Add the table to a JScrollPane for scrollable display
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Display the table in a JOptionPane
        JOptionPane.showMessageDialog(this, scrollPane, "Room Data", JOptionPane.PLAIN_MESSAGE);
    } catch (SQLException ex) {
        // Print stack trace and show an error message to the user if an exception occurs
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to fetch room data.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void displayTablesData() {
    try {
        // Fetch data from the database
        Statement stmt = conn.createStatement();
        ResultSet tableResultSet = stmt.executeQuery("SELECT TableNumber, Capacity, IsOccupied FROM Tables");

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Table Number");
        model.addColumn("Capacity");
        model.addColumn("Status");

        while (tableResultSet.next()) {
            int tableNumber = tableResultSet.getInt("TableNumber");
            int capacity = tableResultSet.getInt("Capacity");
            boolean isOccupied = tableResultSet.getBoolean("IsOccupied");

            String status = isOccupied ? "Occupied" : "Available";

            model.addRow(new Object[]{tableNumber, capacity, status});
        }

        // Close resources
        tableResultSet.close();
        stmt.close();

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JOptionPane.showMessageDialog(this, scrollPane, "Tables Data", JOptionPane.PLAIN_MESSAGE);
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Failed to fetch tables data.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void displayRoomsData() {

        try {
            // Create a statement to execute SQL queries
            Statement stmt = conn.createStatement();
            
            // Execute the SQL query to retrieve data from the database
            ResultSet roomResultSet = stmt.executeQuery("SELECT RoomID, Capacity, IsOccupied FROM Rooms");
            
            // Create a DefaultTableModel to hold the data
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Room ID");
            model.addColumn("Capacity");
            model.addColumn("Is Occupied");
            
            // Iterate through the result set and add data to the table model
            while (roomResultSet.next()) {
                int roomID = roomResultSet.getInt("RoomID");
                int capacity = roomResultSet.getInt("Capacity");
                boolean isOccupied = roomResultSet.getBoolean("IsOccupied");
                
                // Add row to the table model
                model.addRow(new Object[]{roomID, capacity, isOccupied ? "Yes" : "No"});
            }
            
            // Close the ResultSet and Statement
            roomResultSet.close();
            stmt.close();
            
            // Create a JTable with the table model
            JTable table = new JTable(model);
            
            // Add the table to a JScrollPane for scrollable display
            JScrollPane scrollPane = new JScrollPane(table);
            
            // Display the table in a JOptionPane
            JOptionPane.showMessageDialog(this, scrollPane, "Rooms Data", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            // Print stack trace and show an error message to the user if an exception occurs
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch rooms data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void displayDataToFile() {
        try {
            // Create a statement to execute SQL queries
            Statement stmt = conn.createStatement();
            
            // Execute the SQL query to retrieve data from the database
            ResultSet resultSet = stmt.executeQuery("SELECT tables.TableNumber, tables.Capacity, " +
                                                     "orders.CustomerName, food.FoodID, food.Name AS FoodName, food.Price " +
                                                     "FROM Tables tables " +
                                                     "LEFT JOIN Orders orders ON tables.TableID = orders.TableID " +
                                                     "LEFT JOIN Food food ON orders.FoodID = food.FoodID");
            
            // Create a DefaultTableModel to hold the data
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Table Number");
            model.addColumn("Capacity");
            model.addColumn("Customer Name");
            model.addColumn("Food ID");
            model.addColumn("Food Name");
            model.addColumn("Food Price");
            
            // Iterate through the result set and add data to the table model
            while (resultSet.next()) {
                int tableNumber = resultSet.getInt("TableNumber");
                int capacity = resultSet.getInt("Capacity");
                String customerName = resultSet.getString("CustomerName");
                int foodID = resultSet.getInt("FoodID");
                String foodName = resultSet.getString("FoodName");
                double foodPrice = resultSet.getDouble("Price");
                
                // Add row to the table model
                model.addRow(new Object[]{tableNumber, capacity, 
                                           customerName, foodID, foodName, foodPrice});
            }
            
            // Close the ResultSet and Statement
            resultSet.close();
            stmt.close();
            
            // Create a JTable with the table model
            JTable table = new JTable(model);
            
            // Add the table to a JScrollPane for scrollable display
            JScrollPane scrollPane = new JScrollPane(table);
            
            // Set preferred size of scroll pane
            scrollPane.setPreferredSize(new Dimension(1200, 800)); // Set your desired dimensions here
            
            // Display the table in a JOptionPane
            JOptionPane.showMessageDialog(this, scrollPane, "Data Display", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            // Print stack trace and show an error message to the user if an exception occurs
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    private void showFoodDetails() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Food");
    
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Food ID");
            model.addColumn("Food Name");
            model.addColumn("Price");
            model.addColumn("Description");
    
            while (rs.next()) {
                int foodID = rs.getInt("FoodID");
                String foodName = rs.getString("Name");
                double price = rs.getDouble("Price");
                String description = rs.getString("Description");
                model.addRow(new Object[]{foodID, foodName, price, description});
            }
    
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            table.setFillsViewportHeight(true); // Makes the table adjust its size to fill the viewport
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Adjusts column widths automatically
    
            // Set preferred column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(200); // Food ID
            table.getColumnModel().getColumn(1).setPreferredWidth(400); // Food Name
            table.getColumnModel().getColumn(2).setPreferredWidth(200); // Price
            table.getColumnModel().getColumn(3).setPreferredWidth(1000); // Description - increased width
    
            JOptionPane.showMessageDialog(this, scrollPane, "Food Details", JOptionPane.PLAIN_MESSAGE);
    
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch food details.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
  

    public static void main(String[] args) {
        new Receiptionist();
    }
}
