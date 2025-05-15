package team.project;

import java.sql.SQLException; 
import java.util.List;
import java.util.Optional;
public interface UserDAO {
    User create(User user) throws SQLException;
    Optional<User> findById(int id) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findByEmail(String email) throws SQLException;
    Optional<User> findByUsernameOrEmail(String identifier) throws SQLException;
    Optional<User> findByPhoneNumber(String phoneNumber) throws SQLException;
    User update(User user) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    List<User> findAll() throws SQLException;
}

