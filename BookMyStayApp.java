/**
 * Application entry point for the Book My Stay Hotel Booking Management System.
 *
 * All use cases in this learning project are implemented under this same class
 * to keep the execution boundary clear and centralized.
 *
 * @author GHOST-031
 * @version 2.1
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
     * Starts the application and routes execution to a selected use case.
     *
      * @param args command-line arguments (optional: pass UC number like "2")
     */
    public static void main(String[] args) {
          int useCase = 2;

        if (args.length > 0) {
            try {
                useCase = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid use case argument. Running UC02 by default.");
            }
        }

        switch (useCase) {
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
