
package team.project;

import java.time.LocalDateTime;


public class Feedback {
    private int id;            
    private int orderId;
    private int customerId;
    private int rating;         
    private String message;
    private LocalDateTime feedbackDate;

    public Feedback() {
        this.feedbackDate = LocalDateTime.now();
    }

    public Feedback(int orderId, int customerId, int rating, String message) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
        this.setRating(rating); 
        this.message = message;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.rating = rating;
    }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(LocalDateTime feedbackDate) { this.feedbackDate = feedbackDate; }

    @Override
    public String toString() {
        return "Feedback{id=" + id + ", orderId=" + orderId + ", customerId=" + customerId + ", rating=" + rating + "}";
    }
}
