
package team.project; 


import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

public class CartManager {
    private static final Map<Integer, Cart> userCarts = new HashMap<>();
    private final MenuService menuService; 

    public CartManager(MenuService menuService) {
        this.menuService = menuService;
    }

    public Cart getCart(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null to get/create a cart.");
        }
        return userCarts.computeIfAbsent(customer.getId(), k -> new Cart(customer));
    }

    public void addItemToCart(Customer customer, int menuItemId, int quantity)
            throws ResourceNotFoundException, ValidationException, ServiceException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive.");
        }
        Cart cart = getCart(customer);
        Optional<MenuItem> menuItemOpt = menuService.getMenuItemByIdForCustomer(menuItemId); 
        if (menuItemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Menu item with ID " + menuItemId + " not found or not available.");
        }
        MenuItem menuItem = menuItemOpt.get();

        if (quantity > menuItem.getStockLevel()) {
            throw new ValidationException("Not enough stock for " + menuItem.getName() +
                                          ". Available: " + menuItem.getStockLevel() +
                                          ", Requested: " + quantity);
        }
        cart.addItem(menuItem, quantity);
    }


    public void removeItemFromCart(Customer customer, int menuItemId) {
        Cart cart = getCart(customer);
        cart.removeItem(menuItemId);
    }

    public void updateItemQuantityInCart(Customer customer, int menuItemId, int newQuantity)
            throws ResourceNotFoundException, ValidationException, ServiceException {
         if (newQuantity < 0) { 
            throw new ValidationException("New quantity cannot be negative.");
        }
        Cart cart = getCart(customer);
         if (newQuantity == 0) {
            cart.removeItem(menuItemId);
            return;
        }
        Optional<MenuItem> menuItemOpt = menuService.getMenuItemByIdForCustomer(menuItemId);
        if (menuItemOpt.isEmpty()) {
            System.out.println("Item " + menuItemId + " not found in menu to update quantity.");
            return;
        }
         MenuItem menuItem = menuItemOpt.get();
         if (newQuantity > menuItem.getStockLevel()) {
             throw new ValidationException("Not enough stock for " + menuItem.getName() +
                                           ". Available: " + menuItem.getStockLevel() +
                                           ", Requested: " + newQuantity);
         }
        cart.updateItemQuantity(menuItemId, newQuantity);
    }

    public void clearCart(Customer customer) {
        Cart cart = getCart(customer);
        cart.clearCart();
    }

    public void removeCartOnLogout(int customerId) {
        userCarts.remove(customerId);
    }
}