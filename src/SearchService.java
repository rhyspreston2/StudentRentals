import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchService {

    private final StudentRentalsSystem system;

    public SearchService(StudentRentalsSystem system) {
        if (system == null) throw new IllegalArgumentException("System must not be null.");
        this.system = system;
    }

    public List<Room> searchRooms(SearchCriteria criteria) {    // searches rooms based on criteria
        if (criteria == null) throw new IllegalArgumentException("Criteria must not be null.");

        Set<Room> candidates = getInitialCandidates(criteria);  //gets initial candidates for filtering

        List<Room> results = new ArrayList<>();
        for (Room room : candidates) {
            if (!passesPriceFilter(room, criteria)) continue;   //filter by price
            if (!passesDateFilter(room, criteria)) continue;    //filter by date availability
            results.add(room);  //adds leftover rooms to results
        }
        return results;
    }

    private Set<Room> getInitialCandidates(SearchCriteria criteria) {
        // Start from the most selective index
        Set<Room> byCity = null;
        if (criteria.getCityOrArea() != null && !criteria.getCityOrArea().isBlank()) {
            byCity = system.getRoomsByCity(criteria.getCityOrArea());
        }

        Set<Room> byType = null;
        if (criteria.getRoomType() != null) {
            byType = system.getRoomsByType(criteria.getRoomType());
        }

        if (byCity != null && byType != null) {
            Set<Room> intersection = new HashSet<>(byCity);
            intersection.retainAll(byType);
            return intersection;
        }

        if (byCity != null) return new HashSet<>(byCity);
        if (byType != null) return new HashSet<>(byType);

        // If no params, fall back to all rooms
        return new HashSet<>(system.getAllRooms());
    }

    private boolean passesPriceFilter(Room room, SearchCriteria criteria) {
        Integer min = criteria.getMinPrice();
        Integer max = criteria.getMaxPrice();

        if (min != null && room.getMonthlyRent() < min) return false;
        if (max != null && room.getMonthlyRent() > max) return false;
        return true;
    }

    private boolean passesDateFilter(Room room, SearchCriteria criteria) {
        DateRange required = criteria.getRequiredPeriod();
        if (required == null) return true;

        return room.isWithinAvailability(required);
    }
}