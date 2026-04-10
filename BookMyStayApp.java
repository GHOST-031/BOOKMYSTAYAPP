import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Application entry point for the Book My Stay Hotel Booking Management System.
 *
 * All use cases in this learning project are implemented under this same class
 * to keep the execution boundary clear and centralized.
 *
 * @author GHOST-031
 * @version 5.1
 */
public class BookMyStayApp {

    /**
     * Abstract room model shared by all room types.
     */
    private abstract static class Room {
        private final String roomType;
        private final int numberOfBeds;
        private final int sizeInSqFt;
        private final double pricePerNight;

        protected Room(String roomType, int numberOfBeds, int sizeInSqFt, double pricePerNight) {
            this.roomType = roomType;
            this.numberOfBeds = numberOfBeds;
            this.sizeInSqFt = sizeInSqFt;
            this.pricePerNight = pricePerNight;
        }

        public String getRoomType() {
            return roomType;
        }

        public int getNumberOfBeds() {
            return numberOfBeds;
        }

        public int getSizeInSqFt() {
            return sizeInSqFt;
        }

        public double getPricePerNight() {
            return pricePerNight;
        }
    }

    private static class SingleRoom extends Room {
        private SingleRoom() {
            super("Single Room", 1, 180, 2499.0);
        }
    }

    private static class DoubleRoom extends Room {
        private DoubleRoom() {
            super("Double Room", 2, 280, 3999.0);
        }
    }

    private static class SuiteRoom extends Room {
        private SuiteRoom() {
            super("Suite Room", 3, 450, 6999.0);
        }
    }

    /**
     * Centralized room inventory that acts as the single source of truth.
     */
    private static class RoomInventory {
        private final HashMap<String, Integer> availability;

        private RoomInventory() {
            availability = new HashMap<String, Integer>();
            availability.put("Single Room", 8);
            availability.put("Double Room", 5);
            availability.put("Suite Room", 2);
        }

        public int getAvailability(String roomType) {
            Integer count = availability.get(roomType);
            return count == null ? 0 : count;
        }

        public void updateAvailability(String roomType, int newCount) {
            if (newCount < 0) {
                System.out.println("Invalid update for " + roomType + ": availability cannot be negative.");
                return;
            }
            availability.put(roomType, newCount);
        }

        public Map<String, Integer> getInventorySnapshot() {
            return new HashMap<String, Integer>(availability);
        }
    }

    /**
     * Read-only search service for guest-facing room discovery.
     */
    private static class RoomSearchService {
        private final RoomInventory roomInventory;

        private RoomSearchService(RoomInventory roomInventory) {
            this.roomInventory = roomInventory;
        }

        public void displayAvailableRooms(Room[] rooms) {
            for (int index = 0; index < rooms.length; index++) {
                Room room = rooms[index];
                if (room == null) {
                    continue;
                }

                int availableCount = roomInventory.getAvailability(room.getRoomType());
                if (availableCount > 0) {
                    printRoomWithAvailability(room, availableCount);
                }
            }
        }
    }

    /**
     * Booking intent submitted by a guest before allocation.
     */
    private static class Reservation {
        private final String requestId;
        private final String guestName;
        private final String requestedRoomType;
        private final int nights;

        private Reservation(String requestId, String guestName, String requestedRoomType, int nights) {
            this.requestId = requestId;
            this.guestName = guestName;
            this.requestedRoomType = requestedRoomType;
            this.nights = nights;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRequestedRoomType() {
            return requestedRoomType;
        }

        public int getNights() {
            return nights;
        }
    }

    /**
     * Request intake queue preserving first-come-first-served ordering.
     */
    private static class BookingRequestQueue {
        private final Queue<Reservation> pendingRequests;

        private BookingRequestQueue() {
            pendingRequests = new ArrayDeque<Reservation>();
        }

        public void submitRequest(Reservation reservation) {
            if (reservation == null) {
                return;
            }
            pendingRequests.offer(reservation);
        }

        public Queue<Reservation> getQueuedRequestsSnapshot() {
            return new ArrayDeque<Reservation>(pendingRequests);
        }

        public int getPendingRequestCount() {
            return pendingRequests.size();
        }
    }

    /**
     * Starts the application and routes execution to a selected use case.
     *
          * @param args command-line arguments (optional: pass UC number like "5")
     */
    public static void main(String[] args) {
              int useCase = 5;

        if (args.length > 0) {
            try {
                useCase = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid use case argument. Running UC05 by default.");
            }
        }

        switch (useCase) {
            case 5:
                runUseCase5();
                break;
            case 4:
                runUseCase4();
                break;
            case 3:
                runUseCase3();
                break;
            case 2:
                runUseCase2();
                break;
            case 1:
                runUseCase1();
                break;
            default:
                System.out.println("Use case not implemented yet in this class: UC" + useCase);
                break;
        }
    }

    /**
     * Use Case 5: Booking Request (First-Come-First-Served).
     */
    private static void runUseCase5() {
        BookingRequestQueue bookingRequestQueue = new BookingRequestQueue();

        bookingRequestQueue.submitRequest(new Reservation("REQ-1001", "Aarav", "Double Room", 2));
        bookingRequestQueue.submitRequest(new Reservation("REQ-1002", "Diya", "Single Room", 1));
        bookingRequestQueue.submitRequest(new Reservation("REQ-1003", "Kabir", "Suite Room", 3));
        bookingRequestQueue.submitRequest(new Reservation("REQ-1004", "Meera", "Double Room", 2));

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 5.1");
        System.out.println("Use Case: UC05 - Booking Request (First-Come-First-Served)");
        System.out.println();
        System.out.println("Queued Booking Requests (FIFO Order):");

        Queue<Reservation> queuedRequests = bookingRequestQueue.getQueuedRequestsSnapshot();
        while (!queuedRequests.isEmpty()) {
            Reservation reservation = queuedRequests.poll();
            System.out.println(
                reservation.getRequestId()
                    + " | Guest: " + reservation.getGuestName()
                    + " | Room: " + reservation.getRequestedRoomType()
                    + " | Nights: " + reservation.getNights());
        }

        System.out.println();
        System.out.println("Pending requests waiting for allocation: " + bookingRequestQueue.getPendingRequestCount());
        System.out.println("Inventory is unchanged at request intake stage.");
    }

    /**
     * Use Case 4: Room Search and Availability Check.
     */
    private static void runUseCase4() {
        RoomInventory roomInventory = new RoomInventory();

        // Keep one room unavailable to demonstrate filtering behavior.
        roomInventory.updateAvailability("Suite Room", 0);

        Room[] rooms = new Room[] {
            new SingleRoom(),
            new DoubleRoom(),
            new SuiteRoom()
        };

        RoomSearchService searchService = new RoomSearchService(roomInventory);

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 4.1");
        System.out.println("Use Case: UC04 - Room Search and Availability Check");
        System.out.println();
        System.out.println("Available Room Options:");

        searchService.displayAvailableRooms(rooms);

        System.out.println("Search completed with read-only inventory access.");
    }

    /**
     * Use Case 3: Centralized Room Inventory Management.
     */
    private static void runUseCase3() {
        RoomInventory roomInventory = new RoomInventory();

        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 3.1");
        System.out.println("Use Case: UC03 - Centralized Room Inventory Management");
        System.out.println();
        System.out.println("Initial Centralized Inventory:");

        printRoomWithAvailability(singleRoom, roomInventory.getAvailability(singleRoom.getRoomType()));
        printRoomWithAvailability(doubleRoom, roomInventory.getAvailability(doubleRoom.getRoomType()));
        printRoomWithAvailability(suiteRoom, roomInventory.getAvailability(suiteRoom.getRoomType()));

        System.out.println("Applying controlled inventory updates...");
        roomInventory.updateAvailability("Double Room", 4);
        roomInventory.updateAvailability("Suite Room", 1);
        System.out.println();
        System.out.println("Updated Centralized Inventory:");

        printRoomWithAvailability(singleRoom, roomInventory.getAvailability(singleRoom.getRoomType()));
        printRoomWithAvailability(doubleRoom, roomInventory.getAvailability(doubleRoom.getRoomType()));
        printRoomWithAvailability(suiteRoom, roomInventory.getAvailability(suiteRoom.getRoomType()));

        System.out.println("Inventory Snapshot Map: " + roomInventory.getInventorySnapshot());
    }

    /**
     * Use Case 2: Basic Room Types and Static Availability.
     */
    private static void runUseCase2() {
        Room singleRoom = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suiteRoom = new SuiteRoom();

        int singleRoomAvailability = 8;
        int doubleRoomAvailability = 5;
        int suiteRoomAvailability = 2;

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 2.1");
        System.out.println("Use Case: UC02 - Basic Room Types and Static Availability");
        System.out.println();
        System.out.println("Room Inventory Snapshot:");

        printRoomWithAvailability(singleRoom, singleRoomAvailability);
        printRoomWithAvailability(doubleRoom, doubleRoomAvailability);
        printRoomWithAvailability(suiteRoom, suiteRoomAvailability);
    }

    private static void printRoomWithAvailability(Room room, int availability) {
        System.out.println("Type: " + room.getRoomType());
        System.out.println("Beds: " + room.getNumberOfBeds());
        System.out.println("Size (sq ft): " + room.getSizeInSqFt());
        System.out.println("Price per night: INR " + room.getPricePerNight());
        System.out.println("Available rooms: " + availability);
        System.out.println();
    }

    /**
     * Use Case 1: Application Entry and Welcome Message.
     */
    private static void runUseCase1() {
        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: UC01");
    }
}
