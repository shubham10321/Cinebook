package cinema;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cinema.ui.LoginFrame;
import cinema.util.UITheme;

public class Main {
    public static void main(String[] args) {
        UITheme.apply();
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "The application could not start.\n" + e.getMessage(),
                        "CineBook",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
