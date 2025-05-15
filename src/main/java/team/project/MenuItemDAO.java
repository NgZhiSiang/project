package team.project;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
public interface MenuItemDAO {
    MenuItem create(MenuItem menuItem) throws SQLException;
    Optional<MenuItem> findById(int id) throws SQLException;
    List<MenuItem> findAll() throws SQLException;
    List<MenuItem> findByCategory(String categoryName) throws SQLException; 
    List<MenuItem> findByItemType(MenuItemType itemType) throws SQLException;
    Set<String> findAllCategories() throws SQLException; 
    MenuItem update(MenuItem menuItem) throws SQLException; 
    boolean deleteById(int id) throws SQLException;      
    void updateStock(int menuItemId, int quantityChange) throws SQLException;

}
