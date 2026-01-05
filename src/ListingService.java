import java.util.Set;

public class ListingService {

    private final StudentRentalsSystem system;

    public ListingService(StudentRentalsSystem system) {
        if (system == null) throw new IllegalArgumentException("System must not be null.");
        this.system = system;
    }

    public Property addProperty(Homeowner owner, String address, String cityOrArea, String description) {
        if (owner == null) throw new IllegalArgumentException("Owner must not be null.");
        if (!owner.isActive()) throw new IllegalStateException("Homeowner account is deactivated.");

        long propertyId = system.generateId();
        Property property = new Property(propertyId, owner, address, cityOrArea, description);
        system.addProperty(property);
        return property;
    }

    public Room addRoom(Homeowner owner,
                        Property property,
                        RoomType type,
                        int monthlyRent,
                        String description,
                        Set<Amenity> amenities,
                        DateRange availability) {

        if (owner == null || property == null) throw new IllegalArgumentException("Owner/property must not be null.");
        if (property.getOwner().getUserId() != owner.getUserId()) {
            throw new SecurityException("You do not own this property.");
        }

        long roomId = system.generateId();
        Room room = new Room(roomId, property, type, monthlyRent, description, amenities, availability);

        property.addRoom(room);
        system.addRoom(room);
        return room;
    }


    public void removeProperty(Homeowner owner, long propertyId) {
        Property property = system.getPropertyById(propertyId);
        if (property == null) throw new IllegalArgumentException("Property not found: " + propertyId);
        if (property.getOwner().getUserId() != owner.getUserId()) {
            throw new SecurityException("You do not own this property.");
        }
        system.removeProperty(property);
    }


    public void removeRoom(Homeowner owner, long roomId) {
        Room room = system.getRoomById(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found: " + roomId);

        Property property = room.getProperty();
        if (property.getOwner().getUserId() != owner.getUserId()) {
            throw new SecurityException("You do not own the property for this room.");
        }

        // Remove from property collection
        property.removeRoom(room);

        // Remove from system + indexes
        system.removeRoom(room);
    }

    public void updateProperty(Homeowner owner, long propertyId, String newAddress, String newCityOrArea, String newDescription) {
        Property property = system.getPropertyById(propertyId);
        if (property == null) throw new IllegalArgumentException("Property not found: " + propertyId);

        if (property.getOwner().getUserId() != owner.getUserId()) {
            throw new SecurityException("You do not own this property.");
        }

        // If city changes, indexes for existing rooms must be updated.
        String oldCity = property.getCityOrArea();

        if (newAddress != null && !newAddress.isBlank()) property.setAddress(newAddress);
        if (newDescription != null) property.setDescription(newDescription);

        if (newCityOrArea != null && !newCityOrArea.isBlank() && !newCityOrArea.equalsIgnoreCase(oldCity)) {
            // Re-index rooms: remove + re-add each room
            for (Room room : property.getRooms()) {
                system.removeRoom(room);
            }

            property.setCityOrArea(newCityOrArea);

            for (Room room : property.getRooms()) {
                system.addRoom(room);
            }
        }
    }

    public void updateRoom(Homeowner owner,
                           long roomId,
                           Integer newMonthlyRent,
                           String newDescription,
                           Set<Amenity> newAmenities,
                           DateRange newAvailability) {

        Room room = system.getRoomById(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found: " + roomId);

        Property property = room.getProperty();
        if (property.getOwner().getUserId() != owner.getUserId()) {
            throw new SecurityException("You do not own the property for this room.");
        }

        if (newMonthlyRent != null) room.setMonthlyRent(newMonthlyRent);
        if (newDescription != null) room.setDescription(newDescription);
        if (newAmenities != null) room.setAmenities(newAmenities);
        if (newAvailability != null) room.setAvailability(newAvailability);
    }
}