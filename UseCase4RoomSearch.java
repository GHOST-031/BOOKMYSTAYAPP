/**
 * Launcher for Use Case 4 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 4.1
 */
public class UseCase4RoomSearch {

    /**
     * Runs UC04 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"4"});
    }
}
