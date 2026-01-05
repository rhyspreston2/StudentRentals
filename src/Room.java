import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Room {

    private final long roomId;
    private final Property property;
    private final RoomType type;
    private final int monthlyRent;
    private final String description;
    private final Set<Amenity> amenities;
    private final DateRange availability;

    private final List<Booking> bookings;

    public Room(long roomId,
                Property property,
                RoomType type,
                int monthlyRent,
                String description,
                Set<Amenity> amenities,
                DateRange availability) {

        if (property == null)
            throw new IllegalArgumentException("Property must not be null.");
        if (monthlyRent < 0)
            throw new IllegalArgumentException("Rent must not be negative.");
        if (availability == null)
            throw new IllegalArgumentException("Availability must not be null.");

        this.roomId = roomId;
        this.property = property;
        this.type = type;
        this.monthlyRent = monthlyRent;
        this.description = description;
        this.amenities = amenities;
        this.availability = availability;
        this.bookings = new ArrayList<>();
    }

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
}