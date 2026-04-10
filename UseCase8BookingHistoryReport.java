/**
 * Launcher for Use Case 8 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 8.1
 */
public class UseCase8BookingHistoryReport {

    /**
     * Runs UC08 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"8"});
    }
}
