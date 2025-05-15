
package team.project;


import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private final OrderDAO orderDAO;
    private final MenuItemDAO menuItemDAO; 
    private final PaymentDAO paymentDAO;   
    private final CartManager cartManager; 

    public OrderService(OrderDAO orderDAO, MenuItemDAO menuItemDAO, PaymentDAO paymentDAO, CartManager cartManager) {
        this.cartManager = cartManager;
        this.orderDAO = orderDAO;
        this.menuItemDAO = menuItemDAO;
        this.paymentDAO = paymentDAO;
    }


    public Order placeOrder(Customer customer, Cart cart, String deliveryAddress,
                            LocalDateTime deliveryDateTime, String paymentMethod, String specialInstructions)
            throws ValidationException, ServiceException, ResourceNotFoundException, AuthorizationException {

        if (customer == null) {
            throw new AuthorizationException("Customer must be logged in to place an order.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Cannot place an order with an empty cart.");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new ValidationException("Delivery address is required.");
        }

        Order order = new Order(customer, cart, deliveryAddress, deliveryDateTime, specialInstructions);

        for (OrderItem item : order.getItems()) {
            Optional<MenuItem> menuItemOpt;
            try {
                menuItemOpt = menuItemDAO.findById(item.getMenuItem().getId());
            } catch (SQLException e) {
                throw new ServiceException("Failed to retrieve menu item from database: " + e.getMessage(), e);
            }
            if (menuItemOpt.isEmpty() || !menuItemOpt.get().isAvailable()) {
                throw new ResourceNotFoundException("Menu item " + item.getMenuItem().getName() + " is no longer available.");
            }
            MenuItem dbMenuItem = menuItemOpt.get();
            if (item.getQuantity() > dbMenuItem.getStockLevel()) {
                throw new ValidationException("Not enough stock for " + dbMenuItem.getName() +
                                              ". Available: " + dbMenuItem.getStockLevel() +
                                              ", Requested: " + item.getQuantity());
            }
        }
        order.calculateTotals(); 

        Payment payment = new Payment(0, order.getTotalAmount(), paymentMethod); // orderId 稍后设置
        payment.setStatus(PaymentStatus.SUCCESS); 

        try {
            
            Order createdOrder = orderDAO.create(order);
            if (createdOrder.getId() == 0) {
                throw new ServiceException("Failed to save order to database.");
            }

            payment.setOrderId(createdOrder.getId());
            paymentDAO.create(payment);

            for (OrderItem item : createdOrder.getItems()) {
                menuItemDAO.updateStock(item.getMenuItem().getId(), -item.getQuantity()); // 减库存
            }

           
            cartManager.clearCart(customer);

            System.out.println("Order placed successfully! Order ID: " + createdOrder.getId());
            return createdOrder;

        } catch (SQLException e) {
            throw new ServiceException("Failed to place order due to a database error: " + e.getMessage(), e);
        }
    }

    public Optional<Order> getOrderById(int orderId, User requestingUser)
            throws ServiceException, AuthorizationException {
        try {
            Optional<Order> orderOpt = orderDAO.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if (requestingUser.getRole().equals("CUSTOMER")&& order.getCustomerId() != requestingUser.getId()) {
                    throw new AuthorizationException("You are not authorized to view this order.");
                }
                return orderOpt;
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve order: " + e.getMessage(), e);
        }
    }

    public List<Order> getOrderHistoryForCustomer(Customer customer) throws ServiceException {
        if (customer == null) return List.of();
        try {
            return orderDAO.findByCustomerId(customer.getId());
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve order history: " + e.getMessage(), e);
        }
    }

    public boolean updateOrderStatusByAdmin(User adminUser, int orderId, OrderStatus newStatus)
            throws AuthorizationException, ServiceException, ResourceNotFoundException {
        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can update order status.");
        }
        try {
            if (orderDAO.findById(orderId).isEmpty()) {
                 throw new ResourceNotFoundException("Order with ID " + orderId + " not found.");
            }
            return orderDAO.updateStatus(orderId, newStatus);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    public List<Order> getAllOrdersByAdmin(User adminUser) throws AuthorizationException, ServiceException {
         if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can view all orders.");
        }
        try {
            return orderDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve all orders: " + e.getMessage(), e);
        }
    }
}