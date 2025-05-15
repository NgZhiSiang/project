
package team.project;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private int id; 
    private int orderId; 
    private MenuItem menuItem; 
    private int quantity;
    private BigDecimal priceAtPurchase; 
    private BigDecimal subtotal;        
    public OrderItem() {}

    public OrderItem(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null for an OrderItem.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.priceAtPurchase = menuItem.getPrice(); 
        this.calculateSubtotal();
    }

  
    public OrderItem(int id, int orderId, MenuItem menuItem, int quantity, BigDecimal priceAtPurchase) {
        this(menuItem, quantity); 
        this.orderId = orderId;
        this.priceAtPurchase = priceAtPurchase; 
        this.calculateSubtotal(); 
    }


    public void calculateSubtotal() {
        if (this.priceAtPurchase != null && this.quantity > 0) {
            this.subtotal = this.priceAtPurchase.multiply(new BigDecimal(this.quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        if (menuItem != null) { 
            this.priceAtPurchase = menuItem.getPrice();
        }
        calculateSubtotal();
    }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.quantity = quantity;
        calculateSubtotal();
    }
    public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(BigDecimal priceAtPurchase) { 
        this.priceAtPurchase = priceAtPurchase;
        calculateSubtotal();
    }
    public BigDecimal getSubtotal() { return subtotal; }
    
    @Override
    public String toString() {
        return "OrderItem{menuItem=" + (menuItem != null ? menuItem.getName() : "null") +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }

    @Override
    public boolean equals(Object o) { 
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        if (id > 0 && orderItem.id > 0) return id == orderItem.id;
        return Objects.equals(menuItem, orderItem.menuItem);
    }
}