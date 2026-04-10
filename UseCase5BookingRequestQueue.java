/**
 * Launcher for Use Case 5 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 5.1
 */
public class UseCase5BookingRequestQueue {

    /**
     * Runs UC05 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"5"});
    }
}
