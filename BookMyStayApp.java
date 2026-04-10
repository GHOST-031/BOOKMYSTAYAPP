import java.util.HashMap;
import java.util.Map;

/**
 * Application entry point for the Book My Stay Hotel Booking Management System.
 *
 * All use cases in this learning project are implemented under this same class
 * to keep the execution boundary clear and centralized.
 *
 * @author GHOST-031
 * @version 4.1
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
     * Starts the application and routes execution to a selected use case.
     *
          * @param args command-line arguments (optional: pass UC number like "4")
     */
    public static void main(String[] args) {
              int useCase = 4;

        if (args.length > 0) {
            try {
                useCase = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid use case argument. Running UC04 by default.");
            }
        }

        switch (useCase) {
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
