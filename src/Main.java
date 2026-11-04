import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Launch the Welcome / Landing & Authentication Window
            new AuthFrame().setVisible(true);
        });
    }
}