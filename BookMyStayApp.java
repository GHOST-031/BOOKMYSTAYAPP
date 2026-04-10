import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Application entry point for the Book My Stay Hotel Booking Management System.
 *
 * All use cases in this learning project are implemented under this same class
 * to keep the execution boundary clear and centralized.
 *
 * @author GHOST-031
 * @version 12.1
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

        public void restoreFromSnapshot(Map<String, Integer> snapshot) {
            availability.clear();
            if (snapshot != null) {
                availability.putAll(snapshot);
            }
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

        public Reservation dequeueNextRequest() {
            return pendingRequests.poll();
        }

        public boolean hasPendingRequests() {
            return !pendingRequests.isEmpty();
        }
    }

    /**
     * Booking service confirms reservations and allocates unique rooms safely.
     */
    private static class BookingService {
        private final RoomInventory roomInventory;
        private final Set<String> allocatedRoomIds;
        private final HashMap<String, Set<String>> allocatedRoomsByType;
        private int roomSequence;

        private BookingService(RoomInventory roomInventory) {
            this.roomInventory = roomInventory;
            this.allocatedRoomIds = new HashSet<String>();
            this.allocatedRoomsByType = new HashMap<String, Set<String>>();
            this.roomSequence = 100;
        }

        public void processQueuedRequests(BookingRequestQueue bookingRequestQueue) {
            while (bookingRequestQueue.hasPendingRequests()) {
                Reservation reservation = bookingRequestQueue.dequeueNextRequest();
                if (reservation == null) {
                    continue;
                }

                String roomType = reservation.getRequestedRoomType();
                int availableCount = roomInventory.getAvailability(roomType);
                if (availableCount <= 0) {
                    System.out.println("Reservation " + reservation.getRequestId()
                        + " could not be confirmed: no availability for " + roomType + ".");
                    continue;
                }

                String allocatedRoomId = generateUniqueRoomId(roomType);

                // Allocation registration and inventory update are performed together.
                allocatedRoomIds.add(allocatedRoomId);
                Set<String> roomTypeAllocations = allocatedRoomsByType.get(roomType);
                if (roomTypeAllocations == null) {
                    roomTypeAllocations = new HashSet<String>();
                    allocatedRoomsByType.put(roomType, roomTypeAllocations);
                }
                roomTypeAllocations.add(allocatedRoomId);
                roomInventory.updateAvailability(roomType, availableCount - 1);

                System.out.println("Reservation " + reservation.getRequestId()
                    + " confirmed for " + reservation.getGuestName()
                    + ". Allocated Room ID: " + allocatedRoomId);
            }
        }

        public Map<String, Set<String>> getAllocatedRoomsByTypeSnapshot() {
            HashMap<String, Set<String>> snapshot = new HashMap<String, Set<String>>();
            for (Map.Entry<String, Set<String>> entry : allocatedRoomsByType.entrySet()) {
                snapshot.put(entry.getKey(), new HashSet<String>(entry.getValue()));
            }
            return snapshot;
        }

        public int getTotalAllocatedRoomCount() {
            return allocatedRoomIds.size();
        }

        private String generateUniqueRoomId(String roomType) {
            String prefix = roomType.replace(" ", "").toUpperCase();
            String roomId = prefix + "-" + roomSequence;

            while (allocatedRoomIds.contains(roomId)) {
                roomSequence++;
                roomId = prefix + "-" + roomSequence;
            }

            roomSequence++;
            return roomId;
        }
    }

    /**
     * Optional service that can be attached to a reservation.
     */
    private static class AddOnService {
        private final String serviceName;
        private final double serviceCost;

        private AddOnService(String serviceName, double serviceCost) {
            this.serviceName = serviceName;
            this.serviceCost = serviceCost;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getServiceCost() {
            return serviceCost;
        }
    }

    /**
     * Manages reservation-to-add-on mappings independent of core allocation.
     */
    private static class AddOnServiceManager {
        private final HashMap<String, List<AddOnService>> servicesByReservationId;

        private AddOnServiceManager() {
            servicesByReservationId = new HashMap<String, List<AddOnService>>();
        }

        public void addServiceToReservation(String reservationId, AddOnService addOnService) {
            if (reservationId == null || reservationId.isEmpty() || addOnService == null) {
                return;
            }

            List<AddOnService> selectedServices = servicesByReservationId.get(reservationId);
            if (selectedServices == null) {
                selectedServices = new ArrayList<AddOnService>();
                servicesByReservationId.put(reservationId, selectedServices);
            }

            selectedServices.add(addOnService);
        }

        public List<AddOnService> getServicesForReservation(String reservationId) {
            List<AddOnService> selectedServices = servicesByReservationId.get(reservationId);
            if (selectedServices == null) {
                return new ArrayList<AddOnService>();
            }
            return new ArrayList<AddOnService>(selectedServices);
        }

        public double calculateTotalAddOnCost(String reservationId) {
            List<AddOnService> selectedServices = getServicesForReservation(reservationId);
            double totalCost = 0.0;

            for (int index = 0; index < selectedServices.size(); index++) {
                totalCost += selectedServices.get(index).getServiceCost();
            }

            return totalCost;
        }

        public Map<String, List<AddOnService>> getReservationServiceSnapshot() {
            HashMap<String, List<AddOnService>> snapshot = new HashMap<String, List<AddOnService>>();
            for (Map.Entry<String, List<AddOnService>> entry : servicesByReservationId.entrySet()) {
                snapshot.put(entry.getKey(), new ArrayList<AddOnService>(entry.getValue()));
            }
            return snapshot;
        }
    }

    /**
     * Stores confirmed bookings in confirmation order for audit and review.
     */
    private static class BookingHistory {
        private final List<Reservation> confirmedReservations;

        private BookingHistory() {
            confirmedReservations = new ArrayList<Reservation>();
        }

        public void addConfirmedReservation(Reservation reservation) {
            if (reservation != null) {
                confirmedReservations.add(reservation);
            }
        }

        public List<Reservation> getConfirmedReservationsSnapshot() {
            return new ArrayList<Reservation>(confirmedReservations);
        }

        public void restoreFromRecords(List<ReservationRecord> records) {
            confirmedReservations.clear();
            if (records == null) {
                return;
            }

            for (int index = 0; index < records.size(); index++) {
                ReservationRecord record = records.get(index);
                confirmedReservations.add(new Reservation(
                    record.getRequestId(),
                    record.getGuestName(),
                    record.getRequestedRoomType(),
                    record.getNights()));
            }
        }
    }

    /**
     * Generates read-only operational reports from booking history.
     */
    private static class BookingReportService {
        public void printSummaryReport(BookingHistory bookingHistory) {
            List<Reservation> history = bookingHistory.getConfirmedReservationsSnapshot();
            HashMap<String, Integer> countByRoomType = new HashMap<String, Integer>();

            for (int index = 0; index < history.size(); index++) {
                Reservation reservation = history.get(index);
                Integer count = countByRoomType.get(reservation.getRequestedRoomType());
                countByRoomType.put(reservation.getRequestedRoomType(), count == null ? 1 : count + 1);
            }

            System.out.println("Booking Summary Report:");
            System.out.println("Total confirmed reservations: " + history.size());
            System.out.println("Confirmed reservations by room type: " + countByRoomType);
        }
    }

    /**
     * Serializable booking record used for persistence snapshots.
     */
    private static class ReservationRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String requestId;
        private final String guestName;
        private final String requestedRoomType;
        private final int nights;

        private ReservationRecord(String requestId, String guestName, String requestedRoomType, int nights) {
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
     * Serializable snapshot representing durable application state.
     */
    private static class PersistedState implements Serializable {
        private static final long serialVersionUID = 1L;

        private final HashMap<String, Integer> inventorySnapshot;
        private final ArrayList<ReservationRecord> bookingHistoryRecords;

        private PersistedState(Map<String, Integer> inventorySnapshot, List<ReservationRecord> bookingHistoryRecords) {
            this.inventorySnapshot = new HashMap<String, Integer>(inventorySnapshot);
            this.bookingHistoryRecords = new ArrayList<ReservationRecord>(bookingHistoryRecords);
        }

        public Map<String, Integer> getInventorySnapshot() {
            return new HashMap<String, Integer>(inventorySnapshot);
        }

        public List<ReservationRecord> getBookingHistoryRecords() {
            return new ArrayList<ReservationRecord>(bookingHistoryRecords);
        }
    }

    /**
     * Handles durable save and restore of core system state.
     */
    private static class PersistenceService {
        private final String filePath;

        private PersistenceService(String filePath) {
            this.filePath = filePath;
        }

        public void saveState(RoomInventory roomInventory, BookingHistory bookingHistory) {
            List<Reservation> history = bookingHistory.getConfirmedReservationsSnapshot();
            ArrayList<ReservationRecord> records = new ArrayList<ReservationRecord>();

            for (int index = 0; index < history.size(); index++) {
                Reservation reservation = history.get(index);
                records.add(new ReservationRecord(
                    reservation.getRequestId(),
                    reservation.getGuestName(),
                    reservation.getRequestedRoomType(),
                    reservation.getNights()));
            }

            PersistedState state = new PersistedState(roomInventory.getInventorySnapshot(), records);

            try {
                ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath));
                output.writeObject(state);
                output.close();
                System.out.println("State persisted to file: " + filePath);
            } catch (IOException ex) {
                System.out.println("Failed to persist state: " + ex.getMessage());
            }
        }

        public PersistedState loadState(Map<String, Integer> fallbackInventorySnapshot) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Persistence file not found. Bootstrapping safe default state.");
                return new PersistedState(fallbackInventorySnapshot, new ArrayList<ReservationRecord>());
            }

            try {
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath));
                Object stateObject = input.readObject();
                input.close();

                if (stateObject instanceof PersistedState) {
                    System.out.println("State recovered from file: " + filePath);
                    return (PersistedState) stateObject;
                }

                System.out.println("Persistence file format is invalid. Using safe default state.");
            } catch (IOException ex) {
                System.out.println("Failed to read persisted state. Using safe default state. Reason: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("Persisted class metadata mismatch. Using safe default state. Reason: " + ex.getMessage());
            }

            return new PersistedState(fallbackInventorySnapshot, new ArrayList<ReservationRecord>());
        }
    }

    /**
     * Domain-specific exception for invalid booking scenarios.
     */
    private static class InvalidBookingException extends Exception {
        private InvalidBookingException(String message) {
            super(message);
        }
    }

    /**
     * Domain-specific exception for invalid inventory transitions.
     */
    private static class InvalidInventoryStateException extends Exception {
        private InvalidInventoryStateException(String message) {
            super(message);
        }
    }

    /**
     * Validator that protects booking input and inventory state.
     */
    private static class InvalidBookingValidator {
        private final Set<String> supportedRoomTypes;

        private InvalidBookingValidator() {
            supportedRoomTypes = new HashSet<String>();
            supportedRoomTypes.add("Single Room");
            supportedRoomTypes.add("Double Room");
            supportedRoomTypes.add("Suite Room");
        }

        public void validateReservationInput(Reservation reservation) throws InvalidBookingException {
            if (reservation == null) {
                throw new InvalidBookingException("Reservation cannot be null.");
            }

            if (reservation.getRequestId() == null || reservation.getRequestId().isEmpty()) {
                throw new InvalidBookingException("Request ID is required.");
            }

            if (reservation.getGuestName() == null || reservation.getGuestName().isEmpty()) {
                throw new InvalidBookingException("Guest name is required for " + reservation.getRequestId() + ".");
            }

            // Room type comparison is intentionally case-sensitive.
            if (!supportedRoomTypes.contains(reservation.getRequestedRoomType())) {
                throw new InvalidBookingException("Unsupported room type for " + reservation.getRequestId()
                    + ": " + reservation.getRequestedRoomType() + ".");
            }

            if (reservation.getNights() <= 0) {
                throw new InvalidBookingException("Number of nights must be greater than zero for "
                    + reservation.getRequestId() + ".");
            }
        }

        public void validateInventoryTransition(String roomType, int currentCount, int nextCount)
            throws InvalidInventoryStateException {
            if (currentCount < 0) {
                throw new InvalidInventoryStateException("Current inventory is invalid for " + roomType + ".");
            }

            if (nextCount < 0) {
                throw new InvalidInventoryStateException("Inventory cannot become negative for " + roomType + ".");
            }

            if (nextCount > currentCount) {
                throw new InvalidInventoryStateException("Unexpected inventory increase for " + roomType + ".");
            }
        }
    }

    /**
     * Thread-safe queue for concurrent booking request intake.
     */
    private static class SharedBookingQueue {
        private final Queue<Reservation> sharedRequests;

        private SharedBookingQueue() {
            sharedRequests = new ArrayDeque<Reservation>();
        }

        public synchronized void submit(Reservation reservation) {
            if (reservation != null) {
                sharedRequests.offer(reservation);
            }
        }

        public synchronized Reservation dequeue() {
            return sharedRequests.poll();
        }

        public synchronized int size() {
            return sharedRequests.size();
        }
    }

    /**
     * Synchronized processor that protects inventory and allocation state.
     */
    private static class ConcurrentBookingProcessor {
        private final RoomInventory roomInventory;
        private final Set<String> allocatedRoomIds;
        private final HashMap<String, String> reservationToRoomId;
        private int roomSequence;

        private ConcurrentBookingProcessor(RoomInventory roomInventory) {
            this.roomInventory = roomInventory;
            this.allocatedRoomIds = new HashSet<String>();
            this.reservationToRoomId = new HashMap<String, String>();
            this.roomSequence = 500;
        }

        public synchronized boolean allocateSafely(Reservation reservation) {
            if (reservation == null) {
                return false;
            }

            int availableCount = roomInventory.getAvailability(reservation.getRequestedRoomType());
            if (availableCount <= 0) {
                return false;
            }

            String roomId = nextUniqueRoomId(reservation.getRequestedRoomType());
            allocatedRoomIds.add(roomId);
            reservationToRoomId.put(reservation.getRequestId(), roomId);
            roomInventory.updateAvailability(reservation.getRequestedRoomType(), availableCount - 1);
            return true;
        }

        public synchronized Map<String, String> getReservationAllocationSnapshot() {
            return new HashMap<String, String>(reservationToRoomId);
        }

        public synchronized int getAllocatedRoomCount() {
            return allocatedRoomIds.size();
        }

        private String nextUniqueRoomId(String roomType) {
            String prefix = roomType.replace(" ", "").toUpperCase();
            String roomId = prefix + "-" + roomSequence;

            while (allocatedRoomIds.contains(roomId)) {
                roomSequence++;
                roomId = prefix + "-" + roomSequence;
            }

            roomSequence++;
            return roomId;
        }
    }

    /**
     * Starts the application and routes execution to a selected use case.
     *
          * @param args command-line arguments (optional: pass UC number like "12")
     */
    public static void main(String[] args) {
              int useCase = 12;

        if (args.length > 0) {
            try {
                useCase = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid use case argument. Running UC12 by default.");
            }
        }

        switch (useCase) {
            case 12:
                runUseCase12();
                break;
            case 11:
                runUseCase11();
                break;
            case 9:
                runUseCase9();
                break;
            case 8:
                runUseCase8();
                break;
            case 7:
                runUseCase7();
                break;
            case 6:
                runUseCase6();
                break;
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
     * Use Case 12: Data Persistence and System Recovery.
     */
    private static void runUseCase12() {
        String persistenceFilePath = "bookmystay_state.ser";
        PersistenceService persistenceService = new PersistenceService(persistenceFilePath);

        RoomInventory inventoryBeforeShutdown = new RoomInventory();
        BookingHistory historyBeforeShutdown = new BookingHistory();

        historyBeforeShutdown.addConfirmedReservation(new Reservation("RES-12001", "Aarav", "Double Room", 2));
        historyBeforeShutdown.addConfirmedReservation(new Reservation("RES-12002", "Diya", "Suite Room", 1));
        historyBeforeShutdown.addConfirmedReservation(new Reservation("RES-12003", "Kabir", "Single Room", 3));

        inventoryBeforeShutdown.updateAvailability("Double Room", 4);
        inventoryBeforeShutdown.updateAvailability("Suite Room", 1);
        inventoryBeforeShutdown.updateAvailability("Single Room", 7);

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 12.1");
        System.out.println("Use Case: UC12 - Data Persistence and System Recovery");
        System.out.println();
        System.out.println("Preparing for shutdown. Persisting current state...");

        persistenceService.saveState(inventoryBeforeShutdown, historyBeforeShutdown);

        System.out.println();
        System.out.println("Simulating system restart...");

        RoomInventory recoveredInventory = new RoomInventory();
        BookingHistory recoveredHistory = new BookingHistory();

        PersistedState restoredState = persistenceService.loadState(recoveredInventory.getInventorySnapshot());
        recoveredInventory.restoreFromSnapshot(restoredState.getInventorySnapshot());
        recoveredHistory.restoreFromRecords(restoredState.getBookingHistoryRecords());

        System.out.println("Recovery complete. Restored inventory snapshot: " + recoveredInventory.getInventorySnapshot());
        System.out.println("Recovered booking history records:");

        List<Reservation> recoveredReservations = recoveredHistory.getConfirmedReservationsSnapshot();
        for (int index = 0; index < recoveredReservations.size(); index++) {
            Reservation reservation = recoveredReservations.get(index);
            System.out.println(reservation.getRequestId()
                + " | Guest: " + reservation.getGuestName()
                + " | Room: " + reservation.getRequestedRoomType()
                + " | Nights: " + reservation.getNights());
        }

        System.out.println();
        System.out.println("System resumed safely with recovered state.");
    }

    /**
     * Use Case 11: Concurrent Booking Simulation (Thread Safety).
     */
    private static void runUseCase11() {
        final RoomInventory roomInventory = new RoomInventory();
        final SharedBookingQueue sharedBookingQueue = new SharedBookingQueue();
        final ConcurrentBookingProcessor concurrentProcessor = new ConcurrentBookingProcessor(roomInventory);

        Thread guestOne = new Thread(new Runnable() {
            public void run() {
                sharedBookingQueue.submit(new Reservation("REQ-C001", "Aarav", "Double Room", 2));
                sharedBookingQueue.submit(new Reservation("REQ-C002", "Diya", "Suite Room", 1));
            }
        }, "Guest-Producer-1");

        Thread guestTwo = new Thread(new Runnable() {
            public void run() {
                sharedBookingQueue.submit(new Reservation("REQ-C003", "Kabir", "Suite Room", 2));
                sharedBookingQueue.submit(new Reservation("REQ-C004", "Meera", "Suite Room", 1));
            }
        }, "Guest-Producer-2");

        guestOne.start();
        guestTwo.start();

        try {
            guestOne.join();
            guestTwo.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Request intake interrupted: " + ex.getMessage());
        }

        Runnable workerTask = new Runnable() {
            public void run() {
                while (true) {
                    Reservation request = sharedBookingQueue.dequeue();
                    if (request == null) {
                        return;
                    }

                    boolean allocated = concurrentProcessor.allocateSafely(request);
                    if (allocated) {
                        System.out.println(Thread.currentThread().getName() + " allocated request "
                            + request.getRequestId() + " for " + request.getGuestName() + ".");
                    } else {
                        System.out.println(Thread.currentThread().getName() + " could not allocate request "
                            + request.getRequestId() + " (no availability).");
                    }
                }
            }
        };

        Thread workerOne = new Thread(workerTask, "Allocator-1");
        Thread workerTwo = new Thread(workerTask, "Allocator-2");
        Thread workerThree = new Thread(workerTask, "Allocator-3");

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 11.1");
        System.out.println("Use Case: UC11 - Concurrent Booking Simulation (Thread Safety)");
        System.out.println();
        System.out.println("Queued requests before processing: " + sharedBookingQueue.size());
        System.out.println("Starting concurrent allocation threads...");

        workerOne.start();
        workerTwo.start();
        workerThree.start();

        try {
            workerOne.join();
            workerTwo.join();
            workerThree.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Worker processing interrupted: " + ex.getMessage());
        }

        System.out.println();
        System.out.println("All concurrent workers completed.");
        System.out.println("Final reservation-to-room allocation: " + concurrentProcessor.getReservationAllocationSnapshot());
        System.out.println("Total unique rooms allocated: " + concurrentProcessor.getAllocatedRoomCount());
        System.out.println("Final inventory snapshot: " + roomInventory.getInventorySnapshot());
    }

    /**
     * Use Case 9: Error Handling and Validation.
     */
    private static void runUseCase9() {
        RoomInventory roomInventory = new RoomInventory();
        BookingRequestQueue bookingRequestQueue = new BookingRequestQueue();
        InvalidBookingValidator validator = new InvalidBookingValidator();

        bookingRequestQueue.submitRequest(new Reservation("REQ-9001", "Anaya", "Double Room", 2));
        bookingRequestQueue.submitRequest(new Reservation("REQ-9002", "Rohan", "suite room", 1));
        bookingRequestQueue.submitRequest(new Reservation("REQ-9003", "Ira", "Single Room", 0));
        bookingRequestQueue.submitRequest(new Reservation("REQ-9004", "Vihaan", "Suite Room", 1));

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 9.1");
        System.out.println("Use Case: UC09 - Error Handling and Validation");
        System.out.println();
        System.out.println("Processing booking requests with fail-fast validation:");

        while (bookingRequestQueue.hasPendingRequests()) {
            Reservation reservation = bookingRequestQueue.dequeueNextRequest();

            try {
                validator.validateReservationInput(reservation);

                String roomType = reservation.getRequestedRoomType();
                int currentAvailability = roomInventory.getAvailability(roomType);
                int updatedAvailability = currentAvailability - 1;

                validator.validateInventoryTransition(roomType, currentAvailability, updatedAvailability);
                roomInventory.updateAvailability(roomType, updatedAvailability);

                System.out.println("Accepted " + reservation.getRequestId()
                    + " for " + reservation.getGuestName()
                    + ". Updated availability for " + roomType + ": " + updatedAvailability);
            } catch (InvalidBookingException ex) {
                System.out.println("Validation failed: " + ex.getMessage());
            } catch (InvalidInventoryStateException ex) {
                System.out.println("Inventory validation failed: " + ex.getMessage());
            }
        }

        System.out.println();
        System.out.println("System remained stable after validation failures.");
        System.out.println("Final inventory snapshot: " + roomInventory.getInventorySnapshot());
    }

    /**
     * Use Case 8: Booking History and Reporting.
     */
    private static void runUseCase8() {
        BookingHistory bookingHistory = new BookingHistory();
        BookingReportService bookingReportService = new BookingReportService();

        bookingHistory.addConfirmedReservation(new Reservation("RES-8001", "Aarav", "Double Room", 2));
        bookingHistory.addConfirmedReservation(new Reservation("RES-8002", "Diya", "Single Room", 1));
        bookingHistory.addConfirmedReservation(new Reservation("RES-8003", "Kabir", "Suite Room", 3));
        bookingHistory.addConfirmedReservation(new Reservation("RES-8004", "Meera", "Double Room", 2));

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 8.1");
        System.out.println("Use Case: UC08 - Booking History and Reporting");
        System.out.println();
        System.out.println("Confirmed Booking History (chronological order):");

        List<Reservation> history = bookingHistory.getConfirmedReservationsSnapshot();
        for (int index = 0; index < history.size(); index++) {
            Reservation reservation = history.get(index);
            System.out.println(reservation.getRequestId()
                + " | Guest: " + reservation.getGuestName()
                + " | Room: " + reservation.getRequestedRoomType()
                + " | Nights: " + reservation.getNights());
        }

        System.out.println();
        bookingReportService.printSummaryReport(bookingHistory);
        System.out.println("Reporting completed without modifying booking history.");
    }

    /**
     * Use Case 7: Add-On Service Selection.
     */
    private static void runUseCase7() {
        String reservationId = "RES-7001";
        AddOnServiceManager addOnServiceManager = new AddOnServiceManager();

        addOnServiceManager.addServiceToReservation(reservationId, new AddOnService("Airport Pickup", 1200.0));
        addOnServiceManager.addServiceToReservation(reservationId, new AddOnService("Breakfast", 800.0));
        addOnServiceManager.addServiceToReservation(reservationId, new AddOnService("Spa Access", 1500.0));

        List<AddOnService> selectedServices = addOnServiceManager.getServicesForReservation(reservationId);
        double totalAddOnCost = addOnServiceManager.calculateTotalAddOnCost(reservationId);

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 7.1");
        System.out.println("Use Case: UC07 - Add-On Service Selection");
        System.out.println();
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Selected Add-On Services:");

        for (int index = 0; index < selectedServices.size(); index++) {
            AddOnService addOnService = selectedServices.get(index);
            System.out.println("- " + addOnService.getServiceName() + " | Cost: INR " + addOnService.getServiceCost());
        }

        System.out.println();
        System.out.println("Total Additional Cost: INR " + totalAddOnCost);
        System.out.println("Service Mapping Snapshot: " + addOnServiceManager.getReservationServiceSnapshot().keySet());
        System.out.println("Core booking allocation and inventory states remain unchanged.");
    }

    /**
     * Use Case 6: Reservation Confirmation and Room Allocation.
     */
    private static void runUseCase6() {
        RoomInventory roomInventory = new RoomInventory();
        BookingRequestQueue bookingRequestQueue = new BookingRequestQueue();

        bookingRequestQueue.submitRequest(new Reservation("REQ-2001", "Aarav", "Double Room", 2));
        bookingRequestQueue.submitRequest(new Reservation("REQ-2002", "Diya", "Single Room", 1));
        bookingRequestQueue.submitRequest(new Reservation("REQ-2003", "Kabir", "Suite Room", 3));
        bookingRequestQueue.submitRequest(new Reservation("REQ-2004", "Meera", "Suite Room", 2));
        bookingRequestQueue.submitRequest(new Reservation("REQ-2005", "Ishaan", "Suite Room", 1));

        BookingService bookingService = new BookingService(roomInventory);

        System.out.println("Welcome to Book My Stay");
        System.out.println("Application: Hotel Booking Management System");
        System.out.println("Version: 6.1");
        System.out.println("Use Case: UC06 - Reservation Confirmation and Room Allocation");
        System.out.println();
        System.out.println("Processing queued requests in FIFO order:");

        bookingService.processQueuedRequests(bookingRequestQueue);

        System.out.println();
        System.out.println("Allocated Room IDs By Type: " + bookingService.getAllocatedRoomsByTypeSnapshot());
        System.out.println("Total unique allocated room IDs: " + bookingService.getTotalAllocatedRoomCount());
        System.out.println("Inventory after allocation: " + roomInventory.getInventorySnapshot());
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
