/**
 * Application entry point for the Book My Stay Hotel Booking Management System.
 *
 * All use cases in this learning project are implemented under this same class
 * to keep the execution boundary clear and centralized.
 *
 * @author GHOST-031
 * @version UC01
 */
public class BookMyStayApp {

    /**
     * Starts the application and routes execution to a selected use case.
     *
     * @param args command-line arguments (optional: pass UC number like "1")
     */
    public static void main(String[] args) {
        int useCase = 1;

        if (args.length > 0) {
            try {
                useCase = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid use case argument. Running UC01 by default.");
            }
        }

        switch (useCase) {
            case 1:
                runUseCase1();
                break;
            default:
                System.out.println("Use case not implemented yet in this class: UC" + useCase);
                break;
        }
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
