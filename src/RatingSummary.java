public final class RatingSummary {
    private int totalStars;
    private int reviewCount;
    //class to summarise ratings for a property
    // keeps track of total stars and number of reviews to calculate average rating

    public void addRating(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        totalStars += stars;
        reviewCount++;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public double average() {
        if (reviewCount == 0) 
            return 0.0;
        return (double) totalStars / reviewCount;
    }
}
