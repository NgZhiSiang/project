
package team.project;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class MenuService {
    private final MenuItemDAO menuItemDAO;

    public MenuService(MenuItemDAO menuItemDAO) {
        this.menuItemDAO = menuItemDAO;
    }

    public MenuItem addMenuItemByAdmin(User adminUser, String name, String description, BigDecimal price, String category, int initialStock, MenuItemType itemType)
            throws AuthorizationException, ServiceException, ValidationException {

        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can add menu items.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Menu item name cannot be empty.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price cannot be null or negative.");
        }
        if (itemType == null) {
            throw new ValidationException("Menu item type must be specified.");
        }

        MenuItem newItem = new MenuItem(name.trim(), description, price, initialStock,
                                      category != null ? category.trim() : null,
                                      itemType);
        try {
            return menuItemDAO.create(newItem);
        } catch (SQLException e) {
            throw new ServiceException("Failed to add menu item due to a database error.", e);
        }
    }

    public MenuItem updateMenuItemByAdmin(User adminUser, int itemId, String newName, String newDescription,
                                        BigDecimal newPrice, String newCategory, Integer newStockLevel,
                                        MenuItemType newItemType)
            throws AuthorizationException, ServiceException, ValidationException, ResourceNotFoundException {

        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can update menu items.");
        }

        try {
            Optional<MenuItem> existingItemOpt = menuItemDAO.findById(itemId);
            if (existingItemOpt.isEmpty()) {
                throw new ResourceNotFoundException("Menu item with ID " + itemId + " not found.");
            }
            MenuItem itemToUpdate = existingItemOpt.get();


            boolean changed = false;
            if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(itemToUpdate.getName())) {
                itemToUpdate.setName(newName.trim());
                changed = true;
            }
            if (newDescription != null && !newDescription.equals(itemToUpdate.getDescription())) {
                itemToUpdate.setDescription(newDescription);
                changed = true;
            }
            if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) >= 0 && newPrice.compareTo(itemToUpdate.getPrice()) != 0) {
                itemToUpdate.setPrice(newPrice);
                changed = true;
            }
            if (newCategory != null && !newCategory.trim().equals(itemToUpdate.getCategory())) {
                itemToUpdate.setCategory(newCategory.trim());
                changed = true;
            }
            if (newStockLevel != null && newStockLevel >= 0 && newStockLevel != itemToUpdate.getStockLevel()) {
                itemToUpdate.setStockLevel(newStockLevel);
                changed = true;
            }
            if (newItemType != null && newItemType != itemToUpdate.getItemType()) {
                itemToUpdate.setItemType(newItemType);
                changed = true;
            }

            if (changed) {
                return menuItemDAO.update(itemToUpdate);
            }
            return itemToUpdate; 

        } catch (SQLException e) {
            throw new ServiceException("Failed to update menu item due to a database error.", e);
        }
    }

    public boolean deleteMenuItemByAdmin(User adminUser, int itemId)
            throws AuthorizationException, ServiceException, ResourceNotFoundException {
        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can delete menu items.");
        }
        try {
            if (menuItemDAO.findById(itemId).isEmpty()) {
                 throw new ResourceNotFoundException("Menu item with ID " + itemId + " not found for deletion.");
            }
            MenuItem item = menuItemDAO.findById(itemId).get();
            item.setAvailable(false); 
            menuItemDAO.update(item);
            return true;

        } catch (SQLException e) {
            throw new ServiceException("Failed to delete menu item due to a database error.", e);
        }
    }

    public void updateStockByAdmin(User adminUser, int itemId, int quantityChange)
        throws AuthorizationException, ServiceException, ResourceNotFoundException {
        if (adminUser == null || adminUser.getRole().equals("CUSTOMER")) {
            throw new AuthorizationException("Only ADMINs can update stock.");
        }
         try {
            if (menuItemDAO.findById(itemId).isEmpty()) {
                 throw new ResourceNotFoundException("Menu item with ID " + itemId + " not found for stock update.");
            }
            menuItemDAO.updateStock(itemId, quantityChange);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update stock due to a database error.", e);
        }
    }

    public List<MenuItem> getAvailableMenuForCustomer() throws ServiceException {
        try {
            return menuItemDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve available menu items.", e);
        }
    }

    public List<MenuItem> getMenuByCategoryForCustomer(String categoryName) throws ServiceException {
         if (categoryName == null || categoryName.trim().isEmpty()) {
            return getAvailableMenuForCustomer(); 
        }
        try {
            return menuItemDAO.findByCategory(categoryName.trim());
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve menu items by category.", e);
        }
    }

    public Optional<MenuItem> getMenuItemByIdForCustomer(int itemId) throws ServiceException {
        try {
            Optional<MenuItem> itemOpt = menuItemDAO.findById(itemId);
            if (itemOpt.isPresent() && !itemOpt.get().isAvailable()) {
                return Optional.empty();
            }
            return itemOpt;
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve menu item by ID.", e);
        }
    }

    public Set<String> getAvailableCategoriesForCustomer() throws ServiceException {
        try {
            return menuItemDAO.findAllCategories();
        } catch (SQLException e) {
            throw new ServiceException("Failed to retrieve categories.", e);
        }
    }
}