/**
 * Launcher for Use Case 9 that delegates to BookMyStayApp.
 *
 * @author GHOST-031
 * @version 9.1
 */
public class UseCase9ErrorHandlingValidation {

    /**
     * Runs UC09 through the centralized BookMyStayApp entry class.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyStayApp.main(new String[] {"9"});
    }
}
