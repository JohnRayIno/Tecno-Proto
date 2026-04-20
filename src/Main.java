import javax.swing.SwingUtilities;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    SmartStorageUI ui = new SmartStorageUI();
                    ui.setVisible(true);
                    LOGGER.info("Smart Storage Management System started successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing UI", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in main thread", e);
            System.exit(1);
        }
    }
}