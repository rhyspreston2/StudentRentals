import java.util.List;

public class AdminService {

    private final StudentRentalsSystem system; // initialise the system

    public AdminService(StudentRentalsSystem system) {
        if (system == null) throw new IllegalArgumentException("System must not be null.");
        this.system = system;
    }

    public List<User> listUsers() { // lists all users in the system
        return system.getAllUsers();
    }

    public List<Property> listProperties() {    // lists all properties in the system
        return system.getAllProperties();
    }

    public void deactivateUser(Admin admin, long userId) {  // deactivates a user account
        if (admin == null) throw new IllegalArgumentException("Admin must not be null.");

        User user = system.getUserById(userId); // get user by ID
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);

        user.deactivate();  // set user status to DEACTIVATED through User class method
    }

    public void removeProperty(Admin admin, long propertyId) {  // removes a property from the system
        if (admin == null) throw new IllegalArgumentException("Admin must not be null.");

        Property property = system.getPropertyById(propertyId);
        if (property == null) throw new IllegalArgumentException("Property not found: " + propertyId);

        system.removeProperty(property);    // remove property using system method also removing associated rooms
    }
}