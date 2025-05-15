
package team.project;

import java.math.BigDecimal;


public class MenuItem {
    private int id;                 
    private String name;
    private String description;
    private BigDecimal price;
    private int stockLevel;        
    private String category;       
    private MenuItemType itemType;  
    private boolean isAvailable;


    public MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price, int stockLevel, String category, MenuItemType itemType) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockLevel = stockLevel;
        this.category = category;
        this.itemType = itemType;
    }

    public MenuItem(int id, String name, String description, BigDecimal price, int stockLevel, String category, MenuItemType itemType) {
        this(name, description, price, stockLevel, category, itemType);
        this.id = id;
    }


 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public MenuItemType getItemType() { return itemType; }
    public void setItemType(MenuItemType itemType) { this.itemType = itemType; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "MenuItem{id=" + id + ", name='" + name + "', price=" + price + ", type=" + itemType + "}";
    }

}