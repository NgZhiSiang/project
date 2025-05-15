
package team.project;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO;
    private final OrderDAO orderDAO; 

    public FeedbackService(FeedbackDAO feedbackDAO, OrderDAO orderDAO) {
        this.feedbackDAO = feedbackDAO;
        this.orderDAO = orderDAO;
    }

    public Feedback submitFeedback(Customer customer, int orderId, int rating, String message)
            throws ServiceException, ValidationException, ResourceNotFoundException, AuthorizationException {
        if (customer == null) {
            throw new AuthorizationException("Customer must be logged in to submit feedback.");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }

        try {
            Optional<Order> orderOpt = orderDAO.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new ResourceNotFoundException("Order with ID " + orderId + " not found.");
            }
            if (orderOpt.get().getCustomerId() != customer.getId()) {
                throw new AuthorizationException("You can only submit feedback for your own orders.");
            }
            if (feedbackDAO.findByOrderId(orderId).isPresent()) {
                throw new ValidationException("Feedback has already been submitted for this order.");
            }

            Feedback feedback = new Feedback(orderId, customer.getId(), rating, message);
            return feedbackDAO.create(feedback);
        } catch (SQLException e) {
            throw new ServiceException("Failed to submit feedback: " + e.getMessage(), e);
        }
    }

    public Optional<Feedback> getFeedbackByOrderId(int orderId) throws ServiceException {
        try {
            return feedbackDAO.findByOrderId(orderId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve feedback: " + e.getMessage(), e);
        }
    }

    public List<Feedback> getFeedbackByCustomerId(int customerId) throws ServiceException {
        try {
            return feedbackDAO.findByCustomerId(customerId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve feedback for customer: " + e.getMessage(), e);
        }
    }

    public List<Feedback> getAllFeedbackByAdmin(User adminUser) throws AuthorizationException, ServiceException {
        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can view all feedback.");
        }
        try {
            return feedbackDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve all feedback: " + e.getMessage(), e);
        }
    }
}
