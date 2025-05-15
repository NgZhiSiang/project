
package team.project;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Order create(Order order) throws SQLException;

    Optional<Order> findById(int orderId) throws SQLException;
    List<Order> findByCustomerId(int customerId) throws SQLException;
    List<Order> findByStatus(OrderStatus status) throws SQLException; 
    List<Order> findAll() throws SQLException; 

    boolean updateStatus(int orderId, OrderStatus newStatus) throws SQLException;

    
    Order update(Order order) throws SQLException; 

    
}