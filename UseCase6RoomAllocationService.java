/**
 * Launcher for Use Case 6 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 6.1
 */
public class UseCase6RoomAllocationService {

    /**
     * Runs UC06 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"6"});
    }
}
