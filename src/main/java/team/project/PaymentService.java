
package team.project;

import java.sql.SQLException;
import java.util.Optional;

public class PaymentService {
    private final PaymentDAO paymentDAO;

    public PaymentService(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }
    public Payment processPayment(Order order, String paymentMethod) throws ServiceException {
        if (order == null || order.getId() == 0) {
            throw new IllegalArgumentException("Valid order is required for payment processing.");
        }
        Payment payment = new Payment(order.getId(), order.getTotalAmount(), paymentMethod);
        payment.setStatus(PaymentStatus.SUCCESS); 
        payment.setTransactionId("SIM_TRANS_" + System.currentTimeMillis()); // 模拟交易ID

        try {
            
            Optional<Payment> existingPayment = paymentDAO.findByOrderId(order.getId());
            if (existingPayment.isPresent()) {
                System.out.println("Payment record already exists for order ID: " + order.getId());
                return existingPayment.get();
            }
            return paymentDAO.create(payment);
        } catch (SQLException e) {
            throw new ServiceException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    public Optional<Payment> getPaymentDetailsByOrderId(int orderId) throws ServiceException {
        try {
            return paymentDAO.findByOrderId(orderId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve payment details: " + e.getMessage(), e);
        }
    }
}