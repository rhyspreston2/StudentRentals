import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Room {

    private final long roomId;
    private final Property property;
    private final RoomType type;

    private int monthlyRent;
    private String description;
    private Set<Amenity> amenities;
    private DateRange availability; //initialize attributes for Room

    private final List<Booking> bookings;

    public Room(long roomId,    //constructor for Room
                Property property,
                RoomType type,
                int monthlyRent,
                String description,
                Set<Amenity> amenities,
                DateRange availability) {

        if (property == null) throw new IllegalArgumentException("Property must not be null.");
        if (type == null) throw new IllegalArgumentException("Room type must not be null.");
        if (monthlyRent < 0) throw new IllegalArgumentException("Rent must not be negative.");
        if (availability == null) throw new IllegalArgumentException("Availability must not be null.");
        if (amenities == null) throw new IllegalArgumentException("Amenities must not be null.");   //prevents nulls

        this.roomId = roomId;
        this.property = property;
        this.type = type;
        this.monthlyRent = monthlyRent;
        this.description = description;
        this.amenities = amenities;
        this.availability = availability;
        this.bookings = new ArrayList<>();
    }

    //getters

    public long getRoomId() {
        return roomId;
    }

    public Property getProperty() {
        return property;
    }

    public RoomType getType() {
        return type;
    }

    public int getMonthlyRent() {
        return monthlyRent;
    }

    public String getDescription() {
        return description;
    }

    public Set<Amenity> getAmenities() {
        return amenities;
    }

    public DateRange getAvailability() {
        return availability;
    }

    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public boolean isWithinAvailability(DateRange requested) {
        return availability.contains(requested);
    }

    void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public void setMonthlyRent(int monthlyRent) {
        if (monthlyRent < 0) throw new IllegalArgumentException("Rent must not be negative.");
        this.monthlyRent = monthlyRent;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmenities(Set<Amenity> amenities) {
        if (amenities == null) throw new IllegalArgumentException("Amenities must not be null.");
        this.amenities = amenities;
    }

    public void setAvailability(DateRange availability) {
        if (availability == null) throw new IllegalArgumentException("Availability must not be null.");
        this.availability = availability;
    }
}