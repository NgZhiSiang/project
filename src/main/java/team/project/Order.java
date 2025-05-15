// package team.project.model;
package team.project;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;                 
    private int customerId;         
    private List<OrderItem> items; 
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String deliveryAddress; 
    private LocalDateTime deliveryDateTime; 
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;     
    private String specialInstructions;

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now(); 
        this.status = OrderStatus.PENDING;    
        this.subtotalAmount = BigDecimal.ZERO;
        this.deliveryFee = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(Customer customer, Cart cart, String deliveryAddress, LocalDateTime deliveryDateTime, String specialInstructions) {
        this(); 
        if (customer == null || cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Customer and non-empty cart are required to create an order.");
        }
        this.customerId = customer.getId();
        // this.customer = customer;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDateTime = deliveryDateTime;
        this.specialInstructions = specialInstructions;

        for (OrderItem cartItem : cart.getItems()) {
            this.items.add(new OrderItem(cartItem.getMenuItem(), cartItem.getQuantity()));
        }
        this.calculateTotals();
    }


    public void calculateTotals() {
        this.subtotalAmount = BigDecimal.ZERO;
        if (this.items != null) {
            for (OrderItem item : this.items) {
                item.calculateSubtotal(); 
                this.subtotalAmount = this.subtotalAmount.add(item.getSubtotal());
            }
        }
        this.deliveryFee = new BigDecimal("2.00");
        this.totalAmount = this.subtotalAmount.add(this.deliveryFee);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public List<OrderItem> getItems() { return items; } 
    public void setItems(List<OrderItem> items) { 
        this.items = items;
        calculateTotals();
    }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public LocalDateTime getDeliveryDateTime() { return deliveryDateTime; }
    public void setDeliveryDateTime(LocalDateTime deliveryDateTime) { this.deliveryDateTime = deliveryDateTime; }
    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }


    public void addOrderItem(OrderItem item) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
        item.setOrderId(this.id); 
        calculateTotals();
    }


    @Override
    public String toString() {
        return "Order{id=" + id + ", customerId=" + customerId + ", status=" + status + ", totalAmount=" + totalAmount + ", itemsCount=" + (items != null ? items.size() : 0) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }
}