
package team.project; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderItemDAOImpl implements OrderItemDAO {

    private final MenuItemDAO menuItemDAO; 

    public OrderItemDAOImpl() {
        this.menuItemDAO = new MenuItemDAOImpl();
    }

    public OrderItemDAOImpl(MenuItemDAO menuItemDAO) {
        this.menuItemDAO = menuItemDAO;
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        int menuItemId = rs.getInt("menu_item_id");

        Optional<MenuItem> menuItemOptional = menuItemDAO.findById(menuItemId);
        if (menuItemOptional.isEmpty()) {
            throw new SQLException("Could not find MenuItem with ID: " + menuItemId + " referenced in order_items table.");
        }
        MenuItem menuItem = menuItemOptional.get();

        return new OrderItem(
            rs.getInt("id"),
            rs.getInt("order_id"),
            menuItem, 
            rs.getInt("quantity"),
            rs.getBigDecimal("unit_price_at_purchase")
        );
    }

    @Override
    public OrderItem create(Connection conn, OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price_at_purchase, item_subtotal_before_addons) " +
                     "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getMenuItem().getId());
            pstmt.setInt(3, orderItem.getQuantity());
            pstmt.setBigDecimal(4, orderItem.getPriceAtPurchase());
            orderItem.calculateSubtotal(); 
            pstmt.setBigDecimal(5, orderItem.getSubtotal());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderItem.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating order item failed, no ID obtained.");
                }
            }
            return orderItem;
        }
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return items;
    }
}