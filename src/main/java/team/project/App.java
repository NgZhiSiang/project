
package team.project;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App { 

    private static Scanner scanner = new Scanner(System.in);

    private static UserService userService;
    private static MenuService menuService;
    private static CartManager cartManager;
    private static OrderService orderService;
    private static PaymentService paymentService;
    private static FeedbackService feedbackService;

    private static User loggedInUser = null; 

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();
        MenuItemDAO menuItemDAO = new MenuItemDAOImpl();
        OrderItemDAO orderItemDAO = new OrderItemDAOImpl(menuItemDAO); 
        OrderDAO orderDAO = new OrderDAOImpl(orderItemDAO);       
        PaymentDAO paymentDAO = new PaymentDAOImpl();
        FeedbackDAO feedbackDAO = new FeedbackDAOImpl();

        userService = new UserService(userDAO);
        menuService = new MenuService(menuItemDAO);
        cartManager = new CartManager(menuService); 
        orderService = new OrderService(orderDAO, menuItemDAO, paymentDAO, cartManager);
        paymentService = new PaymentService(paymentDAO);
        feedbackService = new FeedbackService(feedbackDAO, orderDAO); 

        runApp();
    }

    private static void runApp() {
        while (true) {
            if (loggedInUser == null) {
                showLoginRegisterMenu();
            } else if (loggedInUser.getRole().equals("CUSTOMER")) {
                showCustomerMenu((Customer) loggedInUser);
            } else if (loggedInUser.getRole().equals("ADMIN")) {
                showAdminMenu((Admin) loggedInUser);
            }
        }
    }

    private static void showLoginRegisterMenu() {
        System.out.println("\n--- Welcome to Burger System ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": handleLogin(); break;
            case "2": handleRegisterCustomer(); break;
            case "3": System.out.println("Exiting..."); System.exit(0); break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void handleLogin() {
        System.out.print("Enter username/email: "); String id = scanner.nextLine();
        System.out.print("Enter password: "); String pw = scanner.nextLine();
        try {
            loggedInUser = userService.login(id, pw);
            System.out.println("Login successful! Welcome " + loggedInUser.getName());
        } catch (AuthenticationException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    private static void handleRegisterCustomer() {
        System.out.print("Enter username: "); String name = scanner.nextLine();
        System.out.print("Enter email: "); String email = scanner.nextLine();
        System.out.print("Enter password: "); String pass = scanner.nextLine();
        System.out.print("Enter phone (optional): "); String phone = scanner.nextLine();
        System.out.print("Enter address (Only for customer): "); String address = scanner.nextLine(); 
        System.out.println("Enter your role: ");
        String role = scanner.nextLine();
        role.toUpperCase();
        if (!role.equals("CUSTOMER") && !role.equals("ADMIN")) {
            System.out.println("Invalid role. Please enter either 'CUSTOMER' or 'ADMIN'.");
            return;
        }
        try {
            userService.register(name, email, pass, phone.isEmpty() ? null : phone, role, address.isEmpty() ? null : address);
            System.out.println("Customer registration successful! Please login.");
        } catch (RegistrationException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }


    private static void showCustomerMenu(Customer customer) {
        System.out.println("\n--- Customer Menu (" + customer.getName() + ") ---");
        System.out.println("1. View Menu & Add to Cart");
        System.out.println("2. View Cart");
        System.out.println("3. Checkout");
        System.out.println("4. View Order History");
        System.out.println("5. Submit Feedback for an Order");
        System.out.println("6. Update Profile");
        System.out.println("7. Logout");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": handleViewMenuAndAddToCart(customer); break;
            case "2": handleViewCart(customer); break;
            case "3": handleCheckout(customer); break;
            case "4": handleViewOrderHistory(customer); break;
            case "5": handleSubmitFeedback(customer); break;
            case "6": handleUpdateCustomerProfile(customer); break;
            case "7": loggedInUser = null; cartManager.removeCartOnLogout(customer.getId()); System.out.println("Logged out."); break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void handleViewMenuAndAddToCart(Customer customer) {
        try {
            System.out.println("\n--- Available Menu ---");
            List<MenuItem> menu = menuService.getAvailableMenuForCustomer();
            if (menu.isEmpty()) {
                System.out.println("Sorry, no items available on the menu right now.");
                return;
            }
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.get(i);
                System.out.printf("%d. %s (%s) - $%.2f [Stock: %d]\n", (i + 1), item.getName(), item.getCategory(), item.getPrice(), item.getStockLevel());
            }
            System.out.print("Enter item number to add to cart (or 0 to go back): ");
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice > 0 && itemChoice <= menu.size()) {
                MenuItem selectedItem = menu.get(itemChoice - 1);
                System.out.print("Enter quantity for " + selectedItem.getName() + ": ");
                int quantity = Integer.parseInt(scanner.nextLine());
                cartManager.addItemToCart(customer, selectedItem.getId(), quantity);
            }
        } catch (ServiceException | ResourceNotFoundException | ValidationException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid input.");
        }
    }

    private static void handleViewCart(Customer customer) {
        Cart cart = cartManager.getCart(customer);
        System.out.println("\n--- Your Cart ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        for (OrderItem item : cart.getItems()) {
            System.out.printf("- %s (x%d) @ $%.2f each = $%.2f\n",
                    item.getMenuItem().getName(), item.getQuantity(), item.getPriceAtPurchase(), item.getSubtotal());
        }
        System.out.printf("Cart Subtotal: $%.2f\n", cart.calculateSubtotal());
        // Options to update quantity or remove items...
    }

    private static void handleCheckout(Customer customer) {
        Cart cart = cartManager.getCart(customer);
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to checkout.");
            return;
        }
        System.out.println("\n--- Checkout ---");
        System.out.println("Current Address: " + (customer.getAddress() != null ? customer.getAddress() : "Not set"));
        System.out.print("Enter delivery address (or press Enter to use current): ");
        String deliveryAddress = scanner.nextLine();
        if (deliveryAddress.isEmpty()) deliveryAddress = customer.getAddress();
        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            System.err.println("Delivery address is required!");
            return;
        }

        
        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1); 
        System.out.print("Enter payment method (e.g., CASH, CARD): ");
        String paymentMethod = scanner.nextLine();
        System.out.print("Any special instructions? (Press Enter if none): ");
        String instructions = scanner.nextLine();

        try {
            Order placedOrder = orderService.placeOrder(customer, cart, deliveryAddress, deliveryTime, paymentMethod, instructions.isEmpty() ? null : instructions);
            System.out.println("Order placed successfully! Order ID: " + placedOrder.getId() + ", Total: $" + placedOrder.getTotalAmount());
        } catch (ValidationException | ServiceException | ResourceNotFoundException | AuthorizationException e) {
            System.err.println("Checkout failed: " + e.getMessage());
        }
    }

    private static void handleViewOrderHistory(Customer customer) {
        try {
            List<Order> orders = orderService.getOrderHistoryForCustomer(customer);
            if (orders.isEmpty()) {
                System.out.println("You have no past orders.");
                return;
            }
            System.out.println("\n--- Your Order History ---");
            for (Order order : orders) {
                System.out.printf("Order ID: %d, Date: %s, Status: %s, Total: $%.2f\n",
                        order.getId(), order.getOrderDate().toLocalDate(), order.getStatus(), order.getTotalAmount());
                // Option to view order details...
            }
        } catch (ServiceException e) {
            System.err.println("Error fetching order history: " + e.getMessage());
        }
    }

     private static void handleSubmitFeedback(Customer customer) {
        System.out.print("Enter Order ID to give feedback for: ");
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            // Verify order belongs to customer and is delivered (additional logic in service if needed)
            Optional<Order> orderOpt = orderService.getOrderById(orderId, customer);
            if (orderOpt.isEmpty() || orderOpt.get().getStatus() != OrderStatus.DELIVERED) {
                 System.out.println("Order not found, does not belong to you, or not yet delivered.");
                 return;
            }

            System.out.print("Enter rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter your comments: ");
            String message = scanner.nextLine();

            feedbackService.submitFeedback(customer, orderId, rating, message);
            System.out.println("Thank you for your feedback!");

        } catch (NumberFormatException e) {
            System.err.println("Invalid Order ID or rating format.");
        } catch (ServiceException | ValidationException | ResourceNotFoundException | AuthorizationException e) {
            System.err.println("Could not submit feedback: " + e.getMessage());
        }
    }


    private static void handleUpdateCustomerProfile(Customer customer) {
        System.out.println("\n--- Update Profile ---");
        System.out.println("Current Name: " + customer.getName());
        System.out.print("Enter new name (or press Enter to keep current): ");
        String newName = scanner.nextLine();

        System.out.println("Current Phone: " + (customer.getPhone() != null ? customer.getPhone() : "Not set"));
        System.out.print("Enter new phone (or press Enter to keep current): ");
        String newPhone = scanner.nextLine();

        System.out.println("Current Address: " + (customer.getAddress() != null ? customer.getAddress() : "Not set"));
        System.out.print("Enter new address (or press Enter to keep current): ");
        String newAddress = scanner.nextLine();

        try {
            User updatedUser = userService.updateProfile(
                customer.getId(),
                newName.isEmpty() ? customer.getName() : newName,
                newPhone.isEmpty() ? customer.getPhone() : newPhone,
                newAddress.isEmpty() ? customer.getAddress() : newAddress
            );
            loggedInUser = updatedUser; 
            System.out.println("Profile updated successfully.");
        } catch (UserNotFoundException | UpdateProfileException e) { 
            System.err.println("Profile update failed: " + e.getMessage());
        } catch (Exception e) { 
             System.err.println("An unexpected error occurred during profile update: " + e.getMessage());
        }
    }


    private static void showAdminMenu(Admin admin) {
        System.out.println("\n--- Admin Menu (" + admin.getName() + ") ---");
        System.out.println("1. Manage Menu Items (Add/Update/Delete)");
        System.out.println("2. View All Orders");
        System.out.println("3. Update Order Status");
        System.out.println("4. View All Users");
        System.out.println("5. View All Feedback");
        // Add more admin functions here...
        System.out.println("6. Logout");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": handleManageMenuItems(admin); break;
            case "2": handleViewAllOrders(admin); break;
            case "3": handleUpdateOrderStatusByAdmin(admin); break;
            case "4": handleViewAllUsers(admin); break;
            case "5": handleViewAllFeedback(admin); break;
            case "6": loggedInUser = null; System.out.println("Logged out."); break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void handleManageMenuItems(Admin admin) {
        System.out.println("TODO: Implement Admin Menu Item Management");
         System.out.print("Action (add/update/delete/list): ");
        String action = scanner.nextLine().toLowerCase();
        try {
            if ("add".equals(action)) {
                System.out.print("Name: "); String name = scanner.nextLine();
                System.out.print("Description: "); String desc = scanner.nextLine();
                System.out.print("Price: "); BigDecimal price = new BigDecimal(scanner.nextLine());
                System.out.print("Category: "); String cat = scanner.nextLine();
                System.out.print("Initial Stock: "); int stock = Integer.parseInt(scanner.nextLine());
                System.out.print("Type (BURGER, SIDE, DRINK): "); MenuItemType type = MenuItemType.valueOf(scanner.nextLine().toUpperCase());
                menuService.addMenuItemByAdmin(admin, name, desc, price, cat, stock, type);
                System.out.println("Menu item added.");
            } else if ("list".equals(action)) {
                 List<MenuItem> allItems = menuService.getAvailableMenuForCustomer(); 
                 System.out.println("\n--- All Menu Items ---");
                 for(MenuItem item : allItems) System.out.println(item.getId() + ": " + item);
            } else if ("update".equals(action)) {
                System.out.print("Enter Menu Item ID to update: ");
                int itemId = Integer.parseInt(scanner.nextLine());
                System.out.print("New Name (or press Enter to skip): "); String newName = scanner.nextLine();
                System.out.print("New Description (or press Enter to skip): "); String newDesc = scanner.nextLine();
                System.out.print("New Price (or press Enter to skip): "); String newPriceStr = scanner.nextLine();
                BigDecimal newPrice = newPriceStr.isEmpty() ? null : new BigDecimal(newPriceStr);
                System.out.print("New Category (or press Enter to skip): "); String newCat = scanner.nextLine();
                System.out.print("New Stock Level (or press Enter to skip): "); String newStockStr = scanner.nextLine();
                Integer newStock = newStockStr.isEmpty() ? null : Integer.parseInt(newStockStr);
                System.out.print("New Type (or press Enter to skip): "); String newTypeStr = scanner.nextLine();
                MenuItemType newType = newTypeStr.isEmpty() ? null : MenuItemType.valueOf(newTypeStr.toUpperCase());
                
                menuService.updateMenuItemByAdmin(admin, itemId, newName, newDesc, newPrice, newCat, newStock, newType);
                System.out.println("Menu item updated.");
            } else if ("delete".equals(action)) {
                 System.out.print("Enter Menu Item ID to delete: ");
                 int itemId = Integer.parseInt(scanner.nextLine());
                 menuService.deleteMenuItemByAdmin(admin, itemId);
                 System.out.println("Menu item deleted.");
            } else {
                 System.err.println("Invalid action.");
            }

        } catch (Exception e) {
            System.err.println("Menu management error: " + e.getMessage());
        }
    }

    private static void handleViewAllOrders(Admin admin) {
        try {
            List<Order> orders = orderService.getAllOrdersByAdmin(admin);
            if (orders.isEmpty()) {
                System.out.println("No orders found.");
                return;
            }
            System.out.println("\n--- All Orders ---");
            for (Order order : orders) {
                 System.out.printf("Order ID: %d, Customer ID: %d, Date: %s, Status: %s, Total: $%.2f\n",
                        order.getId(), order.getCustomerId(), order.getOrderDate().toLocalDate(), order.getStatus(), order.getTotalAmount());
            }
        } catch (ServiceException | AuthorizationException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
     private static void handleUpdateOrderStatusByAdmin(Admin admin) {
        System.out.print("Enter Order ID to update status: ");
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter new status (e.g., PREPARING, OUT_FOR_DELIVERY, DELIVERED): ");
            OrderStatus newStatus = OrderStatus.valueOf(scanner.nextLine().toUpperCase());
            orderService.updateOrderStatusByAdmin(admin, orderId, newStatus);
            System.out.println("Order status updated.");
        } catch (NumberFormatException e) {
            System.err.println("Invalid Order ID or status format.");
        } catch (ServiceException | AuthorizationException | ResourceNotFoundException e) {
             System.err.println("Could not update order status: " + e.getMessage());
        }
    }

    private static void handleViewAllUsers(Admin admin) {
        if (admin.getRole().equals("CUSTOMER")) { System.err.println("Unauthorized"); return; }
        List<User> users = userService.getAllUsers();
        System.out.println("\n--- All Users ---");
        for (User u : users) {
            System.out.println(u);
        }
    }

    private static void handleViewAllFeedback(Admin admin) {
         try {
            List<Feedback> feedbacks = feedbackService.getAllFeedbackByAdmin(admin);
            if (feedbacks.isEmpty()) {
                System.out.println("No feedback submitted yet.");
                return;
            }
            System.out.println("\n--- All Feedback ---");
            for (Feedback fb : feedbacks) {
                 System.out.printf("Feedback ID: %d, Order ID: %d, Customer ID: %d, Rating: %d, Message: %s\n",
                        fb.getId(), fb.getOrderId(), fb.getCustomerId(), fb.getRating(), fb.getMessage());
            }
        } catch (ServiceException | AuthorizationException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}