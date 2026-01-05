import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

public class StudentRentalsCLI {

    private final StudentRentalsSystem system = new StudentRentalsSystem();
    private final SearchService searchService = new SearchService(system);
    private final BookingService bookingService = new BookingService(system);
    private final ReviewService reviewService = new ReviewService(system, bookingService);
    private final AdminService adminService = new AdminService(system);

    // Demo users (selected via menu)
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
                System.out.println("4) Run demo flow (search -> book -> accept -> review)");
                System.out.println("0) Exit");
                System.out.print("Choose an option: ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> studentMenu(sc, demoStudent);
                    case "2" -> homeownerMenu(sc, demoHomeowner);
                    case "3" -> adminMenu(sc, demoAdmin);
                    case "4" -> demoFlow();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            }
        }
    }

    private void seedDemoData() {
        // Create demo users
        demoStudent = new Student(system.generateId(), "Alice Student", "alice@uni.ac.uk",
                "Example University", "S123456", true);
        demoHomeowner = new Homeowner(system.generateId(), "Bob Homeowner", "bob@home.co.uk");
        demoAdmin = new Admin(system.generateId(), "Charlie Admin", "admin@studentrentals.co.uk");

        system.addUser(demoStudent);
        system.addUser(demoHomeowner);
        system.addUser(demoAdmin);

        // Create demo property + rooms
        Property p1 = new Property(system.generateId(), demoHomeowner,
                "12 High Street", "London", "Quiet house near campus");
        system.addProperty(p1);

        Room r1 = new Room(system.generateId(), p1, RoomType.SINGLE, 650,
                "Single room with desk",
                EnumSet.of(Amenity.WIFI, Amenity.DESK, Amenity.BILLS_INCLUDED),
                new DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(120)));
        p1.addRoom(r1);
        system.addRoom(r1);

        Room r2 = new Room(system.generateId(), p1, RoomType.DOUBLE, 850,
                "Double room, bright and spacious",
                EnumSet.of(Amenity.WIFI, Amenity.KITCHEN_ACCESS),
                new DateRange(LocalDate.now().plusDays(10), LocalDate.now().plusDays(200)));
        p1.addRoom(r2);
        system.addRoom(r2);

        Property p2 = new Property(system.generateId(), demoHomeowner,
                "5 River Road", "Manchester", "Great transport links");
        system.addProperty(p2);

        Room r3 = new Room(system.generateId(), p2, RoomType.SINGLE, 550,
                "Affordable single room",
                EnumSet.of(Amenity.WIFI),
                new DateRange(LocalDate.now().plusDays(3), LocalDate.now().plusDays(90)));
        p2.addRoom(r3);
        system.addRoom(r3);
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
            System.out.println("Booking cancelled (if it belonged to you).");
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
            System.out.println("1) View booking requests for my rooms");
            System.out.println("2) Accept booking");
            System.out.println("3) Reject booking");
            System.out.println("4) View all my bookings");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> homeownerViewRequests(homeowner);
                case "2" -> homeownerAccept(sc, homeowner);
                case "3" -> homeownerReject(sc, homeowner);
                case "4" -> printBookings(bookingService.getBookingsForHomeowner(homeowner));
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void homeownerViewRequests(Homeowner homeowner) {
        List<Booking> bookings = bookingService.getBookingsForHomeowner(homeowner);
        System.out.println("\nRequested bookings:");
        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.REQUESTED) {
                System.out.println("Booking#" + b.getBookingId() +
                        " | Room#" + b.getRoom().getRoomId() +
                        " | Student=" + b.getStudent().getName() +
                        " | Period=" + b.getPeriod());
            }
        }
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

    // ---------------- Demo Flow ----------------

    private void demoFlow() {
        System.out.println("\n--- Demo Flow ---");

        // 1) Student searches for a SINGLE in London
        DateRange wanted = new DateRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(30));
        SearchCriteria criteria = new SearchCriteria("London", 0, 800, wanted, RoomType.SINGLE);
        List<Room> rooms = searchService.searchRooms(criteria);

        if (rooms.isEmpty()) {
            System.out.println("No rooms found for demo.");
            return;
        }

        Room chosen = rooms.get(0);
        System.out.println("Student found Room#" + chosen.getRoomId() + " in " + chosen.getProperty().getCityOrArea());

        // 2) Student requests booking
        Booking booking = bookingService.requestBooking(demoStudent, chosen, wanted);
        System.out.println("Booking requested: #" + booking.getBookingId() + " status=" + booking.getStatus());

        // 3) Homeowner accepts
        bookingService.acceptBooking(demoHomeowner, booking.getBookingId());
        System.out.println("Homeowner accepted booking. status=" + booking.getStatus());

        // 4) Fake the booking ended for demo review (set wanted end in past is messy),
        // so just inform the user how review works in real usage.
        System.out.println("NOTE: Reviews require booking end date to have passed.");
        System.out.println("To test review: make a booking whose end date is before today.");

        System.out.println("--- End Demo Flow ---");
    }

    // ---------------- Helpers ----------------

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
}