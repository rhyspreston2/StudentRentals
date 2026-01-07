import java.time.LocalDate;

public class Booking {

    private final long bookingId;
    private final Student student;
    private final Room room;
    private final DateRange period;
    private BookingStatus status;
    private final LocalDate createdAt;  //initialise attributes for booking class

    public Booking(long bookingId,  //constructor for booking class
                   Student student,
                   Room room,
                   DateRange period) {

        if (student == null || room == null || period == null) {
            throw new IllegalArgumentException("Booking fields must not be null.");
        }

        this.bookingId = bookingId;
        this.student = student;
        this.room = room;
        this.period = period;
        this.status = BookingStatus.REQUESTED;  //uses BookingStatus enum for status
        this.createdAt = LocalDate.now();
    }

    //getters and status update methods

    public long getBookingId() {
        return bookingId;
    }

    public Student getStudent() {
        return student;
    }

    public Room getRoom() {
        return room;
    }

    public DateRange getPeriod() {
        return period;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void accept() {
        status = BookingStatus.ACCEPTED;
    }

    public void reject() {
        status = BookingStatus.REJECTED;
    }

    public void cancel() {
        status = BookingStatus.CANCELLED;
    }
}