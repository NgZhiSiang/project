
package team.project;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private List<OrderItem> items;
    private Customer customer; 

    public Cart(Customer customer) {
        this.customer = customer;
        this.items = new ArrayList<>();
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items); 
    }

    public Customer getCustomer() {
        return customer;
    }

    public void addItem(MenuItem menuItem, int quantity) {
        if (menuItem == null || quantity <= 0) {
            System.err.println("Invalid item or quantity to add to cart.");
            return;
        }
        Optional<OrderItem> existingItemOpt = items.stream()
                .filter(oi -> oi.getMenuItem().getId() == menuItem.getId())
                .findFirst();

        if (existingItemOpt.isPresent()) {

            OrderItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            if (quantity > menuItem.getStockLevel()) { 
                System.err.println("Not enough stock for " + menuItem.getName() + ". Available: " + menuItem.getStockLevel());
                return; 
            }
            items.add(new OrderItem(menuItem, quantity));
        }
        System.out.println(quantity + "x " + menuItem.getName() + " added/updated in cart.");
    }

    public void removeItem(int menuItemId) {
        boolean removed = items.removeIf(oi -> oi.getMenuItem().getId() == menuItemId);
        if (removed) {
            System.out.println("Item removed from cart.");
        } else {
            System.out.println("Item not found in cart to remove.");
        }
    }

    public void updateItemQuantity(int menuItemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(menuItemId); 
            return;
        }
        Optional<OrderItem> itemOpt = items.stream()
                .filter(oi -> oi.getMenuItem().getId() == menuItemId)
                .findFirst();

        if (itemOpt.isPresent()) {
            OrderItem item = itemOpt.get();
            if (newQuantity > item.getMenuItem().getStockLevel()) { 
                System.err.println("Not enough stock for " + item.getMenuItem().getName() + ". Available: " + item.getMenuItem().getStockLevel());
                return; 
            }
            item.setQuantity(newQuantity);
            System.out.println("Cart: Quantity updated for " + item.getMenuItem().getName());
        } else {
            System.out.println("Cart: Item not found to update quantity.");
        }
    }

    public void clearCart() {
        items.clear();
        System.out.println("Cart cleared.");
    }

    public BigDecimal calculateSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        return subtotal;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}