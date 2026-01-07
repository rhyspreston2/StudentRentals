public class SearchCriteria {
    private final String cityOrArea;
    private final Integer minPrice;
    private final Integer maxPrice;
    private final DateRange requiredPeriod;
    private final RoomType roomType;    //attributes for search criteria

    public SearchCriteria(String cityOrArea, Integer minPrice, Integer maxPrice,    //constructor for search criteria
                          DateRange requiredPeriod, RoomType roomType) {
        this.cityOrArea = cityOrArea;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.requiredPeriod = requiredPeriod;
        this.roomType = roomType;
    }
    //getters
    public String getCityOrArea() {
        return cityOrArea;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public DateRange getRequiredPeriod() {
        return requiredPeriod;
    }

    public RoomType getRoomType() {
        return roomType;
    }
}