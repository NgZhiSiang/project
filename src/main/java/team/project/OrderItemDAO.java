
package team.project;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface OrderItemDAO {
    OrderItem create(Connection conn, OrderItem orderItem) throws SQLException;
    List<OrderItem> findByOrderId(int orderId) throws SQLException;
}