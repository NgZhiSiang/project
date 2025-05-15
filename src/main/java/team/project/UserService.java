package team.project;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.protobuf.ServiceException;
public class UserService {
     private final UserDAO userDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
    );


    
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }



    public User register(String username, String email, String plainPassword, String phone, String role, String address)
            throws RegistrationException {

        validateRegistrationInput(username, email, plainPassword);

        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim().toLowerCase();

        try {
            if (userDAO.findByUsername(trimmedUsername).isPresent()) {
                throw new RegistrationException("Username '" + trimmedUsername + "' already exists.");
            }
            if (userDAO.findByEmail(trimmedEmail).isPresent()) {
                throw new RegistrationException("Email '" + trimmedEmail + "' is already registered.");
            }


          
            User newUser;
            if (role.equals("CUSTOMER")) {
                newUser = new Customer(trimmedUsername, trimmedEmail, plainPassword, phone, address);
            } else if (role.equals("ADMIN")) {
                if (address != null && !address.isEmpty()) {
                    
                    System.out.println("Warning: Address provided for ADMIN role, will be ignored.");
                }
                newUser = new Admin(trimmedUsername, trimmedEmail, plainPassword, phone);
            } else {
                if (role.equals("CUSTOMER")) {
                    newUser = new Customer(trimmedUsername, trimmedEmail, plainPassword, phone, address);
                } else {
                    throw new RegistrationException("Invalid user role specified for registration.");
                }
            }
            return userDAO.create(newUser);

        } catch (SQLException e) {
            throw new RegistrationException("Failed to register user due to a database error. Please try again later.", e);
        } catch (IllegalArgumentException e) { 
            throw new RegistrationException("Registration failed due to invalid input: " + e.getMessage(), e);
        }
    }

    private void validateRegistrationInput(String username, String email, String password) throws RegistrationException {
        if (username == null || username.trim().isEmpty()) {
            throw new RegistrationException("Username cannot be empty.");
        }
        if (username.trim().length() < 3 || username.trim().length() > 50) {
            throw new RegistrationException("Username must be between 3 and 50 characters.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RegistrationException("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new RegistrationException("Invalid email format.");
        }
        if (password == null || password.isEmpty()) {
            throw new RegistrationException("Password cannot be empty.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
             throw new RegistrationException("Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, and one digit.");

    }
}


    
    public User login(String identifier, String plainPassword) throws AuthenticationException {
        if (identifier == null || identifier.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new AuthenticationException("Username/Email and password cannot be empty.");
        }

        String trimmedIdentifier = identifier.trim();

        try {
            Optional<User> userOptional = userDAO.findByUsernameOrEmail(trimmedIdentifier);

            if (userOptional.isEmpty()) {
                throw new AuthenticationException("Invalid username/email or password.");
            }

            User user = userOptional.get();

            if (!plainPassword.equals(user.getPassword())) {
                throw new AuthenticationException("Invalid username/email or password."); 
            }
            return user;

        } catch (SQLException e) {
            throw new AuthenticationException("Login failed due to a system error. Please try again later.", e);
        }
    }

    public User updateProfile(int userId, String newFullName, String newPhoneNumber,String newAddress)
            throws UserNotFoundException, UpdateProfileException {
        try {
            Optional<User> userOptional = userDAO.findById(userId);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User with ID " + userId + " not found for profile update.");
            }

            User userToUpdate = userOptional.get();
            boolean changed = false;
        
            if (newPhoneNumber != null && !newPhoneNumber.trim().isEmpty() &&
                !newPhoneNumber.trim().equals(userToUpdate.getPhone())) {
                String trimmedNewPhoneNumber = newPhoneNumber.trim();

                Optional<User> existingUserWithPhoneNumber = userDAO.findByPhoneNumber(trimmedNewPhoneNumber);
                if (existingUserWithPhoneNumber.isPresent() && existingUserWithPhoneNumber.get().getId() != userId) {
                    throw new UpdateProfileException("Phone number '" + trimmedNewPhoneNumber + "' is already in use by another account.");
                }
                userToUpdate.setPhone(trimmedNewPhoneNumber);
                changed = true;
            }
             if (userToUpdate instanceof Customer) {
                Customer customerToUpdate = (Customer) userToUpdate;
                if (newAddress != null && !Objects.equals(newAddress, customerToUpdate.getAddress())) { 
                    customerToUpdate.setAddress(newAddress);
                    changed = true;
                }
            } else if (newAddress != null && !newAddress.trim().isEmpty()) {
                System.out.println("Warning: Address update provided for non-CUSTOMER role (ID: " + userId + "), will be ignored.");
            }
            if (changed) {
                return userDAO.update(userToUpdate);
            } else {
                return userToUpdate; 
            }

        } catch (SQLException e) {
            throw new UpdateProfileException("Failed to update profile due to a database error.", e);
        }
    }


    public void changePassword(int userId, String oldPassword, String newPlainPassword)
            throws AuthenticationException, UserNotFoundException, UpdateProfileException {
        if (newPlainPassword == null || newPlainPassword.isEmpty()) {
            throw new UpdateProfileException("New password cannot be empty.");
        }
        if (!PASSWORD_PATTERN.matcher(newPlainPassword).matches()) {
             throw new UpdateProfileException("New password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, and one digit.");
        }

        try {
            Optional<User> userOptional = userDAO.findById(userId);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User with ID " + userId + " not found for password change.");
            }

            User user = userOptional.get();
            if (!oldPassword.equals(user.getPassword())){
                throw new AuthenticationException("Incorrect old password.");
            }

            ((UserDAOImpl) userDAO).updatePassword(userId, newPlainPassword);


        } catch (SQLException e) {

            throw new UpdateProfileException("Failed to change password due to a database error.", e);
        }
    }


    public Optional<User> getUserById(int userId) {
        try {
            return userDAO.findById(userId);
        } catch (SQLException e) {
            
            return Optional.empty();
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            return List.of(); 
        }
    }

    public boolean deleteUser(int userId) throws UserNotFoundException, ServiceException {
        try {
            if (userDAO.findById(userId).isEmpty()) {
                throw new UserNotFoundException("Cannot delete. User with ID " + userId + " not found.");
            }
            return userDAO.deleteById(userId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete user due to a database error.", e);
        }
    }
}
