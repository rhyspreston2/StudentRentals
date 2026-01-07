import java.time.LocalDate;

public class Review {

    private final long reviewId;
    private final Student student;
    private final Property property;
    private final int rating;
    private final String comment;
    private final LocalDate createdAt;  //initialise attributes for review class

    public Review(long reviewId,    //constructor for review class
                  Student student,
                  Property property,
                  int rating,
                  String comment) {

        if (student == null || property == null) {
            throw new IllegalArgumentException("Student and property must not be null.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        this.reviewId = reviewId;
        this.student = student;
        this.property = property;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDate.now();
    }
    //getters

    public long getReviewId() {
        return reviewId;
    }

    public Student getStudent() {
        return student;
    }

    public Property getProperty() {
        return property;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}