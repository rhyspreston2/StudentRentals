public class ReviewService {

    private final StudentRentalsSystem system;
    private final BookingService bookingService;

    public ReviewService(StudentRentalsSystem system, BookingService bookingService) {
        if (system == null || bookingService == null) {
            throw new IllegalArgumentException("System and BookingService must not be null.");
        }
        this.system = system;
        this.bookingService = bookingService;
    }

    public Review leaveReview(Student student,
                              long bookingId,
                              int rating,
                              String comment) {

        if (student == null) {
            throw new IllegalArgumentException("Student must not be null.");
        }

        Booking booking = system.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }

        // Must be the student's own booking
        if (booking.getStudent().getUserId() != student.getUserId()) {
            throw new SecurityException("You can only review your own bookings.");
        }

        // Must be accepted
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED bookings can be reviewed.");
        }

        // Must have ended
        if (!bookingService.hasBookingEnded(booking)) {
            throw new IllegalStateException("You can only review after the booking has ended.");
        }

        Property property = booking.getRoom().getProperty();

        long reviewId = system.generateId();
        Review review = new Review(
                reviewId,
                student,
                property,
                rating,
                comment
        );

        // Store review
        system.addReview(review);
        property.applyReview(review);

        return review;
    }
}