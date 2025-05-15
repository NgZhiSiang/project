
package team.project;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class PaymentDAOImpl implements PaymentDAO {

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        payment.setTransactionId(rs.getString("transaction_id"));
        Timestamp paymentDateTimestamp = rs.getTimestamp("payment_date");
        if (paymentDateTimestamp != null) {
            payment.setPaymentDate(paymentDateTimestamp.toLocalDateTime());
        }
        return payment;
    }

    @Override
    public Payment create(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, status, transaction_id, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getOrderId());
            pstmt.setBigDecimal(2, payment.getAmount());
            pstmt.setString(3, payment.getPaymentMethod());
            pstmt.setString(4, payment.getStatus().name());
            pstmt.setString(5, payment.getTransactionId()); 
            pstmt.setTimestamp(6, payment.getPaymentDate() != null ? Timestamp.valueOf(payment.getPaymentDate()) : Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
            return payment;
        }
    }

    @Override
    public Optional<Payment> findById(int paymentId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paymentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Payment> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Payment update(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET amount = ?, payment_method = ?, status = ?, transaction_id = ?, payment_date = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, payment.getAmount());
            pstmt.setString(2, payment.getPaymentMethod());
            pstmt.setString(3, payment.getStatus().name());
            pstmt.setString(4, payment.getTransactionId());
            pstmt.setTimestamp(5, payment.getPaymentDate() != null ? Timestamp.valueOf(payment.getPaymentDate()) : null);
            pstmt.setInt(6, payment.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating payment failed, no rows affected or payment not found.");
            }
            return payment; 
        }
    }
}
