import java.time.LocalDate;
import java.util.Objects;

public final class DateRange {
    private final LocalDate start;
    private final LocalDate end;

    public DateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before end (range is [start, end)).");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean contains(DateRange other) {
        Objects.requireNonNull(other, "Other range must not be null.");
        return !this.start.isAfter(other.start) && !this.end.isBefore(other.end);
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "Other range must not be null.");
        // overlap iff start < other.end AND end > other.start
        return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }

    @Override
    public String toString() {
        return "[" + start + " to " + end + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateRange)) return false;
        DateRange that = (DateRange) o;
        return start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}