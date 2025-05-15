
package team.project;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDAOImpl implements FeedbackDAO {

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setId(rs.getInt("id"));
        feedback.setOrderId(rs.getInt("order_id"));
        feedback.setCustomerId(rs.getInt("customer_id"));
        feedback.setRating(rs.getInt("rating"));
        feedback.setMessage(rs.getString("message"));
        Timestamp feedbackDateTimestamp = rs.getTimestamp("feedback_date");
        if (feedbackDateTimestamp != null) {
            feedback.setFeedbackDate(feedbackDateTimestamp.toLocalDateTime());
        }
        return feedback;
    }

    @Override
    public Feedback create(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (order_id, customer_id, rating, message, feedback_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, feedback.getOrderId());
            pstmt.setInt(2, feedback.getCustomerId());
            pstmt.setInt(3, feedback.getRating());
            pstmt.setString(4, feedback.getMessage());
            pstmt.setTimestamp(5, feedback.getFeedbackDate() != null ? Timestamp.valueOf(feedback.getFeedbackDate()) : Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating feedback failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating feedback failed, no ID obtained.");
                }
            }
            return feedback;
        }
    }

    @Override
    public Optional<Feedback> findById(int feedbackId) throws SQLException {
        String sql = "SELECT * FROM feedback WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, feedbackId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Feedback> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM feedback WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Feedback> findByCustomerId(int customerId) throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT * FROM feedback WHERE customer_id = ? ORDER BY feedback_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(mapResultSetToFeedback(rs));
                }
            }
        }
        return feedbacks;
    }

    @Override
    public List<Feedback> findAll() throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String sql = "SELECT * FROM feedback ORDER BY feedback_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                feedbacks.add(mapResultSetToFeedback(rs));
            }
        }
        return feedbacks;
    }
}