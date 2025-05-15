
package team.project;

import java.sql.SQLException;
import java.util.Optional;

public interface PaymentDAO {
    Payment create(Payment payment) throws SQLException;
    Optional<Payment> findById(int paymentId) throws SQLException;
    Optional<Payment> findByOrderId(int orderId) throws SQLException; // 一个订单通常只有一个支付
    Payment update(Payment payment) throws SQLException; // 例如更新状态或 transactionId
}
