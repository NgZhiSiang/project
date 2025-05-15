
package team.project;



import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private final OrderItemDAO orderItemDAO;

    public OrderDAOImpl() {
        this.orderItemDAO = new OrderItemDAOImpl();
    }

    public OrderDAOImpl(OrderItemDAO orderItemDAO) {
        this.orderItemDAO = orderItemDAO;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));

        Timestamp orderDateTimestamp = rs.getTimestamp("order_date");
        if (orderDateTimestamp != null) {
            order.setOrderDate(orderDateTimestamp.toLocalDateTime());
        }
        order.setDeliveryAddress(rs.getString("delivery_address"));
        Timestamp deliveryDateTimeTimestamp = rs.getTimestamp("requested_delivery_datetime");
        if (deliveryDateTimeTimestamp != null) {
            order.setDeliveryDateTime(deliveryDateTimeTimestamp.toLocalDateTime());
        }
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setSubtotalAmount(rs.getBigDecimal("subtotal_amount"));
        order.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
        order.setTotalAmount(rs.getBigDecimal("total_price"));
        order.setSpecialInstructions(rs.getString("special_instructions"));

        
        List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
        order.setItems(items); 

        return order;
    }

    @Override
    public Order create(Order order) throws SQLException {
        String sqlOrder = "INSERT INTO orders (customer_id, delivery_address, requested_delivery_datetime, status, " +
                          "subtotal_amount, delivery_fee, total_price, special_instructions, order_date) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                pstmtOrder.setInt(1, order.getCustomerId());
                pstmtOrder.setString(2, order.getDeliveryAddress());
                pstmtOrder.setTimestamp(3, order.getDeliveryDateTime() != null ? Timestamp.valueOf(order.getDeliveryDateTime()) : null);
                pstmtOrder.setString(4, order.getStatus().name());
                order.calculateTotals();
                pstmtOrder.setBigDecimal(5, order.getSubtotalAmount());
                pstmtOrder.setBigDecimal(6, order.getDeliveryFee());
                pstmtOrder.setBigDecimal(7, order.getTotalAmount());
                pstmtOrder.setString(8, order.getSpecialInstructions());
                pstmtOrder.setTimestamp(9, order.getOrderDate() != null ? Timestamp.valueOf(order.getOrderDate()) : Timestamp.valueOf(LocalDateTime.now()));


                int affectedRows = pstmtOrder.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmtOrder.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained for order.");
                    }
                }
            }

            // 2. 插入 OrderItems
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    item.setOrderId(order.getId()); 
                    orderItemDAO.create(conn,item); 
                }
            }
            conn.commit(); 
            return order; 

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException ex) {
                }
            }
            throw e; 
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }


    @Override
    public Optional<Order> findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }


    @Override
    public boolean updateStatus(int orderId, OrderStatus newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setInt(2, orderId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public Order update(Order order) throws SQLException {
        String sql = "UPDATE orders SET delivery_address = ?, delivery_datetime = ?, status = ?, " +
                     "subtotal_amount = ?, delivery_fee = ?, total_price = ?, " +
                     "special_instructions = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, order.getDeliveryAddress());
            pstmt.setTimestamp(2, order.getDeliveryDateTime() != null ? Timestamp.valueOf(order.getDeliveryDateTime()) : null);
            pstmt.setString(3, order.getStatus().name());
            order.calculateTotals(); 
            pstmt.setBigDecimal(4, order.getSubtotalAmount());
            pstmt.setBigDecimal(5, order.getDeliveryFee());
            pstmt.setBigDecimal(6, order.getTotalAmount());
            pstmt.setString(7, order.getSpecialInstructions());
            pstmt.setInt(8, order.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating order failed, no rows affected or order not found.");
            }
            return findById(order.getId()).orElseThrow(() -> new SQLException("Failed to retrieve updated order."));
        }
    }
}