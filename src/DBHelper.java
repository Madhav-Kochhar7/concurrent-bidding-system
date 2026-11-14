import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBHelper {
    // Database connection constants (replace with your MySQL credentials)
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static int saveItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (name, start_price, min_increment) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getStartPrice());
            stmt.setInt(3, item.getMinIncrement());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    item.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    public static ArrayList<Item> getAllItems() throws SQLException {
        ArrayList<Item> list = new ArrayList<>();
        String sql = "SELECT item_id, name, start_price, min_increment, winner, final_price FROM items ORDER BY item_id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Item item = new Item(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getInt("start_price"),
                    rs.getInt("min_increment")
                );
                item.setWinner(rs.getString("winner"));
                item.setFinalPrice(rs.getInt("final_price"));
                list.add(item);
            }
        }
        return list;
    }
}
