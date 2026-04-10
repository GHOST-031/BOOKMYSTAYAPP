/**
 * Launcher for Use Case 7 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 7.1
 */
public class UseCase7AddOnServiceSelection {

    /**
     * Runs UC07 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"7"});
    }
}
