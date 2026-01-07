import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Property {

    private final long propertyId;
    private final Homeowner owner;
    private String address;
    private String cityOrArea;
    private String description;

    private final List<Room> rooms;
    private final RatingSummary ratingSummary;

    public Property(long propertyId,
                    Homeowner owner,
                    String address,
                    String cityOrArea,
                    String description) {

        if (owner == null) throw new IllegalArgumentException("Owner must not be null.");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address must not be blank.");
        if (cityOrArea == null || cityOrArea.isBlank()) throw new IllegalArgumentException("City/Area must not be blank.");

        this.propertyId = propertyId;
        this.owner = owner;
        this.address = address;
        this.cityOrArea = cityOrArea;
        this.description = description;
        this.rooms = new ArrayList<>();
        this.ratingSummary = new RatingSummary();
    }

    //getters and setters
    public long getPropertyId() {
        return propertyId;
    }

    public Homeowner getOwner() {
        return owner;
    }

    public String getAddress() {
        return address;
    }

    public String getCityOrArea() {
        return cityOrArea;
    }

    public String getDescription() {
        return description;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public RatingSummary getRatingSummary() {
        return ratingSummary;
    }

    public void addRoom(Room room) {
        if (room == null) throw new IllegalArgumentException("Room must not be null.");
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public void applyReview(Review review) {
        ratingSummary.addRating(review.getRating());
    }

    public double getAverageRating() {
        return ratingSummary.average();
    }

    public void setAddress(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("Address must not be blank.");
        this.address = address;
    }

    public void setCityOrArea(String cityOrArea) {
        if (cityOrArea == null || cityOrArea.isBlank()) throw new IllegalArgumentException("City/Area must not be blank.");
        this.cityOrArea = cityOrArea;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}