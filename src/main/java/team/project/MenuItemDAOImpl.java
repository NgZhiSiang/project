
package team.project;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MenuItemDAOImpl implements MenuItemDAO {

    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setCategory(rs.getString("category")); 
        item.setStockLevel(rs.getInt("stock_level"));
        item.setItemType(MenuItemType.valueOf(rs.getString("item_type")));
         item.setAvailable(true);
        return item;
    }

   

    @Override
    public MenuItem create(MenuItem menuItem) throws SQLException {
        String sql = "INSERT INTO menu_items (name, description, price, category, stock_level, item_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setBigDecimal(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());
            pstmt.setInt(5, menuItem.getStockLevel());
            pstmt.setString(6, menuItem.getItemType().name());
    

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating menu item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menuItem.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating menu item failed, no ID obtained.");
                }
            }
            return menuItem; 
        }
    }

    @Override
    public Optional<MenuItem> findById(int id) throws SQLException {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMenuItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<MenuItem> findAll() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToMenuItem(rs));
            }
        }
        return items;
    }


    @Override
    public List<MenuItem> findByCategory(String categoryName) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE category = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToMenuItem(rs));
                }
            }
        }
        return items;
    }


    @Override
    public List<MenuItem> findByItemType(MenuItemType itemType) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE item_type = ?  ORDER BY category, name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemType.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToMenuItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public Set<String> findAllCategories() throws SQLException {
        Set<String> categories = new HashSet<>();
        String sql = "SELECT DISTINCT category FROM menu_items WHERE category IS NOT NULL AND category != '' ORDER BY category";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }


    @Override
    public MenuItem update(MenuItem menuItem) throws SQLException {
        String sql = "UPDATE menu_items SET name = ?, description = ?, price = ?, category = ?, " +
                     "stock_level = ?, item_type = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setBigDecimal(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());
            pstmt.setInt(5, menuItem.getStockLevel());
            pstmt.setString(6, menuItem.getItemType().name());
            pstmt.setInt(7, menuItem.getId()); 

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating menu item failed, no rows affected or item not found.");
            }
            return menuItem; 
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public void updateStock(int menuItemId, int quantityChange) throws SQLException {
        String sql = "UPDATE menu_items SET stock_level = stock_level + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, menuItemId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating stock failed, menu item with id " + menuItemId + " not found.");
            }
            // 可以在这里添加检查 stock_level 是否低于 low_stock_threshold 并触发警报的逻辑
            // Optional<MenuItem> updatedItem = findById(menuItemId);
            // if (updatedItem.isPresent() && updatedItem.get().getStockLevel() < updatedItem.get().getLowStockThreshold()) {
            //     System.out.println("ALERT: Stock for " + updatedItem.get().getName() + " is low!");
            //     // 实际应用中会调用 NotificationService
            // }
        }
    }
}