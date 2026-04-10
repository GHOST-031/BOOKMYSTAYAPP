/**
 * Launcher for Use Case 12 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 12.1
 */
public class UseCase12DataPersistenceRecovery {

    /**
     * Runs UC12 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"12"});
    }
}
