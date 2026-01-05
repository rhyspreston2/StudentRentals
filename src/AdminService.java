import java.util.List;

public class AdminService {

    private final StudentRentalsSystem system;

    public AdminService(StudentRentalsSystem system) {
        if (system == null) throw new IllegalArgumentException("System must not be null.");
        this.system = system;
    }

    public List<User> listUsers() {
        return system.getAllUsers();
    }

    public List<Property> listProperties() {
        return system.getAllProperties();
    }

    public void deactivateUser(Admin admin, long userId) {
        if (admin == null) throw new IllegalArgumentException("Admin must not be null.");

        User user = system.getUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);

        user.deactivate();
    }

    public void removeProperty(Admin admin, long propertyId) {
        if (admin == null) throw new IllegalArgumentException("Admin must not be null.");

        Property property = system.getPropertyById(propertyId);
        if (property == null) throw new IllegalArgumentException("Property not found: " + propertyId);

        system.removeProperty(property);
    }
}