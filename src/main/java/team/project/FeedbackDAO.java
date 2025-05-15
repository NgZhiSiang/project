
package team.project;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface FeedbackDAO {
    Feedback create(Feedback feedback) throws SQLException;
    Optional<Feedback> findById(int feedbackId) throws SQLException;
    Optional<Feedback> findByOrderId(int orderId) throws SQLException;
    List<Feedback> findByCustomerId(int customerId) throws SQLException; 
    List<Feedback> findAll() throws SQLException; 
}