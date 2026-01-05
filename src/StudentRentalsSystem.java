import java.util.*;

/**
 * In-memory data store + ID generation + basic indexes.
 * No external libraries, CLI-friendly, designed to be used by services.
 */
public class StudentRentalsSystem {

    // --- ID generation ---
    private long nextId = 1;

    public long generateId() {
        return nextId++;
    }

    // --- Core storage ---
    private final Map<Long, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    private final Map<Long, Property> propertiesById = new HashMap<>();
    private final Map<Long, Room> roomsById = new HashMap<>();
    private final Map<Long, Booking> bookingsById = new HashMap<>();
    private final Map<Long, Review> reviewsById = new HashMap<>();

    // --- Search indexes (for efficiency) ---
    // cityOrArea (lowercased) -> rooms
    private final Map<String, Set<Room>> roomsByCity = new HashMap<>();
    // type -> rooms
    private final Map<RoomType, Set<Room>> roomsByType = new HashMap<>();

    // ---------------- USERS ----------------

    public void addUser(User user) {
        Objects.requireNonNull(user, "User must not be null.");
        if (usersById.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("Duplicate userId: " + user.getUserId());
        }
        String emailKey = user.getEmail().toLowerCase();
        if (usersByEmail.containsKey(emailKey)) {
            throw new IllegalArgumentException("Email already in use: " + user.getEmail());
        }

        usersById.put(user.getUserId(), user);
        usersByEmail.put(emailKey, user);
    }

    public User getUserById(long userId) {
        return usersById.get(userId);
    }

    public User getUserByEmail(String email) {
        if (email == null) return null;
        return usersByEmail.get(email.toLowerCase());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    // ---------------- PROPERTIES ----------------

    public void addProperty(Property property) {
        Objects.requireNonNull(property, "Property must not be null.");
        if (propertiesById.containsKey(property.getPropertyId())) {
            throw new IllegalArgumentException("Duplicate propertyId: " + property.getPropertyId());
        }
        propertiesById.put(property.getPropertyId(), property);
    }

    public Property getPropertyById(long propertyId) {
        return propertiesById.get(propertyId);
    }

    public List<Property> getAllProperties() {
        return new ArrayList<>(propertiesById.values());
    }

    public void removeProperty(Property property) {
        Objects.requireNonNull(property, "Property must not be null.");

        // Remove rooms belonging to this property from indexes and rooms map
        for (Room room : property.getRooms()) {
            removeRoom(room);
        }

        propertiesById.remove(property.getPropertyId());
    }

    // ---------------- ROOMS ----------------

    public void addRoom(Room room) {
        Objects.requireNonNull(room, "Room must not be null.");
        if (roomsById.containsKey(room.getRoomId())) {
            throw new IllegalArgumentException("Duplicate roomId: " + room.getRoomId());
        }

        roomsById.put(room.getRoomId(), room);

        // Index by city
        String cityKey = room.getProperty().getCityOrArea().toLowerCase();
        roomsByCity.computeIfAbsent(cityKey, k -> new HashSet<>()).add(room);

        // Index by type
        roomsByType.computeIfAbsent(room.getType(), k -> new HashSet<>()).add(room);
    }

    public Room getRoomById(long roomId) {
        return roomsById.get(roomId);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(roomsById.values());
    }

    public Set<Room> getRoomsByCity(String cityOrArea) {
        if (cityOrArea == null) return Set.of();
        return roomsByCity.getOrDefault(cityOrArea.toLowerCase(), Set.of());
    }

    public Set<Room> getRoomsByType(RoomType type) {
        if (type == null) return Set.of();
        return roomsByType.getOrDefault(type, Set.of());
    }

    public void removeRoom(Room room) {
        Objects.requireNonNull(room, "Room must not be null.");

        roomsById.remove(room.getRoomId());

        // Remove from indexes
        String cityKey = room.getProperty().getCityOrArea().toLowerCase();
        Set<Room> citySet = roomsByCity.get(cityKey);
        if (citySet != null) {
            citySet.remove(room);
            if (citySet.isEmpty()) roomsByCity.remove(cityKey);
        }

        Set<Room> typeSet = roomsByType.get(room.getType());
        if (typeSet != null) {
            typeSet.remove(room);
            if (typeSet.isEmpty()) roomsByType.remove(room.getType());
        }
    }

    // ---------------- BOOKINGS ----------------

    public void addBooking(Booking booking) {
        Objects.requireNonNull(booking, "Booking must not be null.");
        if (bookingsById.containsKey(booking.getBookingId())) {
            throw new IllegalArgumentException("Duplicate bookingId: " + booking.getBookingId());
        }
        bookingsById.put(booking.getBookingId(), booking);
    }

    public Booking getBookingById(long bookingId) {
        return bookingsById.get(bookingId);
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingsById.values());
    }

    // ---------------- REVIEWS ----------------

    public void addReview(Review review) {
        Objects.requireNonNull(review, "Review must not be null.");
        if (reviewsById.containsKey(review.getReviewId())) {
            throw new IllegalArgumentException("Duplicate reviewId: " + review.getReviewId());
        }
        reviewsById.put(review.getReviewId(), review);
    }

    public Review getReviewById(long reviewId) {
        return reviewsById.get(reviewId);
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviewsById.values());
    }
}