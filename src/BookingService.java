import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService {

    private final StudentRentalsSystem system;

    public BookingService(StudentRentalsSystem system) {
        if (system == null) throw new IllegalArgumentException("System must not be null.");
        this.system = system;
    }

    /**
     * Student requests to book a room within the room's availability window.
     * Creates a booking with status REQUESTED.
     */
    public Booking requestBooking(Student student, Room room, DateRange period) {
        if (student == null || room == null || period == null) {
            throw new IllegalArgumentException("Student, room, and period must not be null.");
        }
        if (!student.isActive()) {
            throw new IllegalStateException("Student account is deactivated.");
        }

        // Must fit within the room’s overall availability window
        if (!room.isWithinAvailability(period)) {
            throw new IllegalArgumentException("Requested dates are outside the room's availability window.");
        }

        // If there is already an ACCEPTED booking that overlaps, reject the request immediately
        if (!isRoomFree(room, period)) {
            throw new IllegalStateException("Room is not available for the requested dates.");
        }

        long bookingId = system.generateId();
        Booking booking = new Booking(bookingId, student, room, period);

        // Save booking
        system.addBooking(booking);

        // Link booking to room (Room keeps list for quick checks)
        room.addBooking(booking);

        return booking;
    }

    /**
     * Homeowner accepts a booking request for a room they own.
     * Re-checks overlap at accept time to prevent double booking.
     */
    public void acceptBooking(Homeowner homeowner, long bookingId) {
        Booking booking = requireBooking(bookingId);

        if (homeowner == null) throw new IllegalArgumentException("Homeowner must not be null.");
        if (!homeowner.isActive()) throw new IllegalStateException("Homeowner account is deactivated.");

        Room room = booking.getRoom();
        Property property = room.getProperty();

        // Ensure this homeowner owns the property
        if (property.getOwner().getUserId() != homeowner.getUserId()) {
            throw new SecurityException("You do not own the property for this booking.");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED bookings can be accepted.");
        }

        // Critical: re-check room availability against ACCEPTED bookings
        if (!isRoomFree(room, booking.getPeriod())) {
            booking.reject(); // safe fallback to avoid double booking
            throw new IllegalStateException("Cannot accept: room conflicts with an existing accepted booking.");
        }

        booking.accept();
    }

    /**
     * Homeowner rejects a booking request for a room they own.
     */
    public void rejectBooking(Homeowner homeowner, long bookingId) {
        Booking booking = requireBooking(bookingId);

        if (homeowner == null) throw new IllegalArgumentException("Homeowner must not be null.");

        Room room = booking.getRoom();
        Property property = room.getProperty();

        if (property.getOwner().getUserId() != homeowner.getUserId()) {
            throw new SecurityException("You do not own the property for this booking.");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED bookings can be rejected.");
        }

        booking.reject();
    }

    /**
     * Student cancels their booking (prototype: no penalties).
     */
    public void cancelBooking(Student student, long bookingId) {
        Booking booking = requireBooking(bookingId);

        if (student == null) throw new IllegalArgumentException("Student must not be null.");
        if (booking.getStudent().getUserId() != student.getUserId()) {
            throw new SecurityException("You can only cancel your own bookings.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) return;

        booking.cancel();
    }

    /**
     * Checks if a room is free for the given period considering ACCEPTED bookings only.
     * REQUESTED bookings do not block availability until accepted.
     */
    public boolean isRoomFree(Room room, DateRange requested) {
        for (Booking existing : room.getBookings()) {
            if (existing.getStatus() != BookingStatus.ACCEPTED) continue;
            if (existing.getPeriod().overlaps(requested)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Student: view their bookings.
     */
    public List<Booking> getBookingsForStudent(Student student) {
        List<Booking> results = new ArrayList<>();
        for (Booking b : system.getAllBookings()) {
            if (b.getStudent().getUserId() == student.getUserId()) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Homeowner: view bookings for rooms they own.
     */
    public List<Booking> getBookingsForHomeowner(Homeowner homeowner) {
        List<Booking> results = new ArrayList<>();
        for (Booking b : system.getAllBookings()) {
            Property p = b.getRoom().getProperty();
            if (p.getOwner().getUserId() == homeowner.getUserId()) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Useful for reviews: booking must have ended.
     */
    public boolean hasBookingEnded(Booking booking) {
        LocalDate today = LocalDate.now();
        // Using [start, end): booking ended if end is on/before today
        return !booking.getPeriod().getEnd().isAfter(today);
    }

    private Booking requireBooking(long bookingId) {
        Booking booking = system.getBookingById(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found: " + bookingId);
        return booking;
    }
}