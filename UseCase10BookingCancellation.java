/**
 * Launcher for Use Case 10 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 10.1
 */
public class UseCase10BookingCancellation {

    /**
     * Runs UC10 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"10"});
    }
}
