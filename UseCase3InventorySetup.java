/**
 * Launcher for Use Case 3 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 3.1
 */
public class UseCase3InventorySetup {

    /**
     * Runs UC03 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"3"});
    }
}
