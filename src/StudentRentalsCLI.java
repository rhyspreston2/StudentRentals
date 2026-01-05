import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class StudentRentalsCLI {

    private final StudentRentalsSystem system = new StudentRentalsSystem();

    private final SearchService searchService = new SearchService(system);
    private final BookingService bookingService = new BookingService(system);
    private final ReviewService reviewService = new ReviewService(system, bookingService);
    private final AdminService adminService = new AdminService(system);
    private final ListingService listingService = new ListingService(system);

    private Student demoStudent;
    private Homeowner demoHomeowner;
    private Admin demoAdmin;

    public static void main(String[] args) {
        new StudentRentalsCLI().run();
    }

    private void run() {
        seedDemoData();

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\n=== StudentRentals (CLI) ===");
                System.out.println("1) Continue as Student");
                System.out.println("2) Continue as Homeowner");
                System.out.println("3) Continue as Admin");
                System.out.println("0) Exit");
                System.out.print("Choose an option: ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> studentMenu(sc, demoStudent);
                    case "2" -> homeownerMenu(sc, demoHomeowner);
                    case "3" -> adminMenu(sc, demoAdmin);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            }
        }
    }

    private void seedDemoData() {
        demoStudent = new Student(system.generateId(), "Rhys Preston", "prestonr@cardiff.ac.uk",
                "Cardiff University", "C24030492", true);
        demoHomeowner = new Homeowner(system.generateId(), "Homeowner", "homeowner@example.com");
        demoAdmin = new Admin(system.generateId(), "CPS Homes", "admin@CPS.co.uk");

        system.addUser(demoStudent);
        system.addUser(demoHomeowner);
        system.addUser(demoAdmin);

    }

    // ---------------- Student Menu ----------------

    private void studentMenu(Scanner sc, Student student) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Student Menu (" + student.getName() + ") ---");
            System.out.println("1) Search rooms");
            System.out.println("2) View my bookings");
            System.out.println("3) Cancel a booking");
            System.out.println("4) Leave a review");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> studentSearchAndRequest(sc, student);
                case "2" -> printBookings(bookingService.getBookingsForStudent(student));
                case "3" -> studentCancelBooking(sc, student);
                case "4" -> studentLeaveReview(sc, student);
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void studentSearchAndRequest(Scanner sc, Student student) {
        System.out.print("City/Area (e.g., London): ");
        String city = sc.nextLine().trim();

        System.out.print("Min price (blank for none): ");
        Integer min = parseIntegerOrNull(sc.nextLine().trim());

        System.out.print("Max price (blank for none): ");
        Integer max = parseIntegerOrNull(sc.nextLine().trim());

        System.out.print("Room type (SINGLE/DOUBLE or blank): ");
        RoomType type = parseRoomTypeOrNull(sc.nextLine().trim());

        System.out.print("Move-in date (YYYY-MM-DD) or blank: ");
        LocalDate start = parseDateOrNull(sc.nextLine().trim());

        System.out.print("Move-out date (YYYY-MM-DD) or blank: ");
        LocalDate end = parseDateOrNull(sc.nextLine().trim());

        DateRange required = null;
        if (start != null && end != null) {
            required = new DateRange(start, end);
        }

        SearchCriteria criteria = new SearchCriteria(city, min, max, required, type);
        List<Room> results = searchService.searchRooms(criteria);

        if (results.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        System.out.println("\nSearch results:");
        for (int i = 0; i < results.size(); i++) {
            Room r = results.get(i);
            Property p = r.getProperty();
            System.out.println((i + 1) + ") Room#" + r.getRoomId() +
                    " | " + p.getCityOrArea() +
                    " | " + r.getType() +
                    " | £" + r.getMonthlyRent() +
                    " | Property avg rating: " + String.format("%.2f", p.getAverageRating()));
        }

        System.out.print("Select a room number to request booking (0 to cancel): ");
        int idx = parseInt(sc.nextLine().trim(), 0);
        if (idx <= 0 || idx > results.size()) return;

        Room chosen = results.get(idx - 1);

        if (required == null) {
            System.out.println("You must provide move-in and move-out dates to request a booking.");
            return;
        }

        try {
            Booking b = bookingService.requestBooking(student, chosen, required);
            System.out.println("Booking requested! Booking ID: " + b.getBookingId() + " (status: " + b.getStatus() + ")");
        } catch (Exception e) {
            System.out.println("Booking request failed: " + e.getMessage());
        }
    }

    private void studentCancelBooking(Scanner sc, Student student) {
        printBookings(bookingService.getBookingsForStudent(student));
        System.out.print("Enter bookingId to cancel: ");
        long id = parseLong(sc.nextLine().trim(), -1);
        if (id <= 0) return;

        try {
            bookingService.cancelBooking(student, id);
            System.out.println("Booking cancelled.");
        } catch (Exception e) {
            System.out.println("Cancel failed: " + e.getMessage());
        }
    }

    private void studentLeaveReview(Scanner sc, Student student) {
        printBookings(bookingService.getBookingsForStudent(student));
        System.out.print("Enter bookingId to review: ");
        long bookingId = parseLong(sc.nextLine().trim(), -1);
        if (bookingId <= 0) return;

        System.out.print("Rating (1-5): ");
        int rating = parseInt(sc.nextLine().trim(), -1);

        System.out.print("Comment: ");
        String comment = sc.nextLine();

        try {
            Review review = reviewService.leaveReview(student, bookingId, rating, comment);
            System.out.println("Review submitted. Review ID: " + review.getReviewId());
            System.out.println("Property new average: " +
                    String.format("%.2f", review.getProperty().getAverageRating()));
        } catch (Exception e) {
            System.out.println("Review failed: " + e.getMessage());
        }
    }

    // ---------------- Homeowner Menu ----------------

    private void homeownerMenu(Scanner sc, Homeowner homeowner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Homeowner Menu (" + homeowner.getName() + ") ---");
            System.out.println("1) List a new property");
            System.out.println("2) View my properties/rooms");
            System.out.println("3) Add a room to one of my properties");
            System.out.println("4) Update a property");
            System.out.println("5) Update a room");
            System.out.println("6) Remove a property");
            System.out.println("7) Remove a room");
            System.out.println("8) View booking requests");
            System.out.println("9) Accept booking");
            System.out.println("10) Reject booking");
            System.out.println("11) View all my bookings");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> homeownerAddProperty(sc, homeowner);
                case "2" -> homeownerViewMyListings(homeowner);
                case "3" -> homeownerAddRoom(sc, homeowner);
                case "4" -> homeownerUpdateProperty(sc, homeowner);
                case "5" -> homeownerUpdateRoom(sc, homeowner);
                case "6" -> homeownerRemoveProperty(sc, homeowner);
                case "7" -> homeownerRemoveRoom(sc, homeowner);
                case "8" -> homeownerViewRequests(homeowner);
                case "9" -> homeownerAccept(sc, homeowner);
                case "10" -> homeownerReject(sc, homeowner);
                case "11" -> printBookings(bookingService.getBookingsForHomeowner(homeowner));
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private List<Property> getMyProperties(Homeowner homeowner) {
        return system.getAllProperties().stream()
                .filter(p -> p.getOwner().getUserId() == homeowner.getUserId())
                .toList();
    }

    private void homeownerAddProperty(Scanner sc, Homeowner homeowner) {
        System.out.print("Address: ");
        String address = sc.nextLine().trim();

        System.out.print("City/Area: ");
        String city = sc.nextLine().trim();

        System.out.print("Description (optional): ");
        String desc = sc.nextLine().trim();

        try {
            Property p = listingService.addProperty(homeowner, address, city, desc);
            System.out.println("Property listed! Property ID: " + p.getPropertyId());
        } catch (Exception e) {
            System.out.println("Failed to add property: " + e.getMessage());
        }
    }

    private void homeownerViewMyListings(Homeowner homeowner) {
        List<Property> props = getMyProperties(homeowner);
        if (props.isEmpty()) {
            System.out.println("You have no properties yet.");
            return;
        }

        System.out.println("\nMy Properties:");
        for (Property p : props) {
            System.out.println("Property#" + p.getPropertyId() + " | " + p.getCityOrArea() +
                    " | " + p.getAddress() + " | Rooms=" + p.getRooms().size() +
                    " | AvgRating=" + String.format("%.2f", p.getAverageRating()));
            for (Room r : p.getRooms()) {
                System.out.println("  - Room#" + r.getRoomId() + " | " + r.getType() +
                        " | £" + r.getMonthlyRent() +
                        " | Availability=" + r.getAvailability() +
                        " | Amenities=" + r.getAmenities());
            }
        }
    }

    private Property chooseProperty(Scanner sc, Homeowner homeowner) {
        List<Property> props = getMyProperties(homeowner);
        if (props.isEmpty()) {
            System.out.println("You have no properties.");
            return null;
        }

        System.out.println("\nSelect a property:");
        for (int i = 0; i < props.size(); i++) {
            Property p = props.get(i);
            System.out.println((i + 1) + ") Property#" + p.getPropertyId() + " | " + p.getCityOrArea() + " | " + p.getAddress());
        }

        System.out.print("Enter number: ");
        int idx = parseInt(sc.nextLine().trim(), -1);
        if (idx < 1 || idx > props.size()) return null;

        return props.get(idx - 1);
    }

    private Room chooseRoom(Scanner sc, Homeowner homeowner) {
        Property p = chooseProperty(sc, homeowner);
        if (p == null) return null;

        if (p.getRooms().isEmpty()) {
            System.out.println("This property has no rooms.");
            return null;
        }

        System.out.println("\nSelect a room:");
        List<Room> rooms = p.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            System.out.println((i + 1) + ") Room#" + r.getRoomId() + " | " + r.getType() + " | £" + r.getMonthlyRent());
        }

        System.out.print("Enter number: ");
        int idx = parseInt(sc.nextLine().trim(), -1);
        if (idx < 1 || idx > rooms.size()) return null;

        return rooms.get(idx - 1);
    }

    private void homeownerAddRoom(Scanner sc, Homeowner homeowner) {
        Property property = chooseProperty(sc, homeowner);
        if (property == null) return;

        System.out.print("Room type (SINGLE/DOUBLE): ");
        RoomType type = RoomType.valueOf(sc.nextLine().trim().toUpperCase());

        System.out.print("Monthly rent (number): ");
        int rent = parseInt(sc.nextLine().trim(), -1);
        if (rent < 0) {
            System.out.println("Invalid rent.");
            return;
        }

        System.out.print("Room description (optional): ");
        String desc = sc.nextLine().trim();

        System.out.print("Amenities (comma separated, e.g. WIFI,DESK) or blank: ");
        String amenityLine = sc.nextLine().trim();
        Set<Amenity> amenities = parseAmenities(amenityLine);

        System.out.print("Available from (YYYY-MM-DD): ");
        LocalDate from = LocalDate.parse(sc.nextLine().trim());

        System.out.print("Available to (YYYY-MM-DD): ");
        LocalDate to = LocalDate.parse(sc.nextLine().trim());

        try {
            DateRange availability = new DateRange(from, to);
            Room room = listingService.addRoom(homeowner, property, type, rent, desc, amenities, availability);
            System.out.println("Room added! Room ID: " + room.getRoomId());
        } catch (Exception e) {
            System.out.println("Failed to add room: " + e.getMessage());
        }
    }

    private void homeownerUpdateProperty(Scanner sc, Homeowner homeowner) {
        Property property = chooseProperty(sc, homeowner);
        if (property == null) return;

        System.out.println("Leave blank to keep current value.");
        System.out.print("New address (current: " + property.getAddress() + "): ");
        String address = sc.nextLine();

        System.out.print("New city/area (current: " + property.getCityOrArea() + "): ");
        String city = sc.nextLine();

        System.out.print("New description (current: " + property.getDescription() + "): ");
        String desc = sc.nextLine();

        try {
            listingService.updateProperty(homeowner, property.getPropertyId(), address, city, desc);
            System.out.println("Property updated.");
        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    private void homeownerUpdateRoom(Scanner sc, Homeowner homeowner) {
        Room room = chooseRoom(sc, homeowner);
        if (room == null) return;

        System.out.println("Leave blank to keep current value.");

        System.out.print("New monthly rent (current: " + room.getMonthlyRent() + "): ");
        String rentStr = sc.nextLine().trim();
        Integer rent = rentStr.isBlank() ? null : parseInt(rentStr, -1);

        System.out.print("New description (current: " + room.getDescription() + "): ");
        String desc = sc.nextLine();
        if (desc != null && desc.isBlank()) desc = null; // treat blank as "no change"

        System.out.print("New amenities (comma separated, current: " + room.getAmenities() + "): ");
        String amenityLine = sc.nextLine().trim();
        Set<Amenity> amenities = amenityLine.isBlank() ? null : parseAmenities(amenityLine);

        System.out.print("New available from (YYYY-MM-DD) or blank (current: " + room.getAvailability().getStart() + "): ");
        LocalDate from = parseDateOrNull(sc.nextLine().trim());

        System.out.print("New available to (YYYY-MM-DD) or blank (current: " + room.getAvailability().getEnd() + "): ");
        LocalDate to = parseDateOrNull(sc.nextLine().trim());

        DateRange newRange = null;
        if (from != null && to != null) {
            newRange = new DateRange(from, to);
        }

        try {
            listingService.updateRoom(homeowner, room.getRoomId(), rent, desc, amenities, newRange);
            System.out.println("Room updated.");
        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    private void homeownerRemoveProperty(Scanner sc, Homeowner homeowner) {
        Property property = chooseProperty(sc, homeowner);
        if (property == null) return;

        try {
            listingService.removeProperty(homeowner, property.getPropertyId());
            System.out.println("Property removed.");
        } catch (Exception e) {
            System.out.println("Remove failed: " + e.getMessage());
        }
    }

    private void homeownerRemoveRoom(Scanner sc, Homeowner homeowner) {
        Room room = chooseRoom(sc, homeowner);
        if (room == null) return;

        try {
            listingService.removeRoom(homeowner, room.getRoomId());
            System.out.println("Room removed.");
        } catch (Exception e) {
            System.out.println("Remove failed: " + e.getMessage());
        }
    }

    private void homeownerViewRequests(Homeowner homeowner) {
        List<Booking> bookings = bookingService.getBookingsForHomeowner(homeowner);
        System.out.println("\nRequested bookings:");
        boolean any = false;
        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.REQUESTED) {
                any = true;
                System.out.println("Booking#" + b.getBookingId() +
                        " | Room#" + b.getRoom().getRoomId() +
                        " | Student=" + b.getStudent().getName() +
                        " | Period=" + b.getPeriod());
            }
        }
        if (!any) System.out.println("(none)");
    }

    private void homeownerAccept(Scanner sc, Homeowner homeowner) {
        homeownerViewRequests(homeowner);
        System.out.print("Enter bookingId to accept: ");
        long id = parseLong(sc.nextLine().trim(), -1);
        if (id <= 0) return;

        try {
            bookingService.acceptBooking(homeowner, id);
            System.out.println("Booking accepted.");
        } catch (Exception e) {
            System.out.println("Accept failed: " + e.getMessage());
        }
    }

    private void homeownerReject(Scanner sc, Homeowner homeowner) {
        homeownerViewRequests(homeowner);
        System.out.print("Enter bookingId to reject: ");
        long id = parseLong(sc.nextLine().trim(), -1);
        if (id <= 0) return;

        try {
            bookingService.rejectBooking(homeowner, id);
            System.out.println("Booking rejected.");
        } catch (Exception e) {
            System.out.println("Reject failed: " + e.getMessage());
        }
    }

    // ---------------- Admin Menu ----------------

    private void adminMenu(Scanner sc, Admin admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Menu (" + admin.getName() + ") ---");
            System.out.println("1) List users");
            System.out.println("2) List properties");
            System.out.println("3) Deactivate user");
            System.out.println("4) Remove property");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> listUsers();
                case "2" -> listProperties();
                case "3" -> adminDeactivate(sc, admin);
                case "4" -> adminRemoveProperty(sc, admin);
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void listUsers() {
        System.out.println("\nUsers:");
        for (User u : adminService.listUsers()) {
            System.out.println("User#" + u.getUserId() + " | " + u);
        }
    }

    private void listProperties() {
        System.out.println("\nProperties:");
        for (Property p : adminService.listProperties()) {
            System.out.println("Property#" + p.getPropertyId() +
                    " | Owner=" + p.getOwner().getName() +
                    " | " + p.getCityOrArea() +
                    " | AvgRating=" + String.format("%.2f", p.getAverageRating()) +
                    " | Rooms=" + p.getRooms().size());
        }
    }

    private void adminDeactivate(Scanner sc, Admin admin) {
        listUsers();
        System.out.print("Enter userId to deactivate: ");
        long id = parseLong(sc.nextLine().trim(), -1);
        if (id <= 0) return;

        try {
            adminService.deactivateUser(admin, id);
            System.out.println("User deactivated.");
        } catch (Exception e) {
            System.out.println("Deactivate failed: " + e.getMessage());
        }
    }

    private void adminRemoveProperty(Scanner sc, Admin admin) {
        listProperties();
        System.out.print("Enter propertyId to remove: ");
        long id = parseLong(sc.nextLine().trim(), -1);
        if (id <= 0) return;

        try {
            adminService.removeProperty(admin, id);
            System.out.println("Property removed.");
        } catch (Exception e) {
            System.out.println("Remove failed: " + e.getMessage());
        }
    }


    private void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("No bookings.");
            return;
        }
        System.out.println("\nBookings:");
        for (Booking b : bookings) {
            System.out.println("Booking#" + b.getBookingId() +
                    " | Status=" + b.getStatus() +
                    " | Room#" + b.getRoom().getRoomId() +
                    " | Student=" + b.getStudent().getName() +
                    " | Period=" + b.getPeriod() +
                    " | City=" + b.getRoom().getProperty().getCityOrArea());
        }
    }

    private Integer parseIntegerOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return fallback; }
    }

    private long parseLong(String s, long fallback) {
        try { return Long.parseLong(s); }
        catch (NumberFormatException e) { return fallback; }
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }

    private RoomType parseRoomTypeOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return RoomType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private Set<Amenity> parseAmenities(String line) {
        if (line == null || line.isBlank()) return EnumSet.noneOf(Amenity.class);

        EnumSet<Amenity> set = EnumSet.noneOf(Amenity.class);
        String[] parts = line.split(",");
        for (String p : parts) {
            String token = p.trim();
            if (token.isEmpty()) continue;
            try {
                set.add(Amenity.valueOf(token.toUpperCase()));
            } catch (Exception ignored) {
                System.out.println("Unknown amenity ignored: " + token);
            }
        }
        return set;
    }
}