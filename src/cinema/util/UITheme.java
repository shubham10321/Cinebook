package cinema.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Shared colours and fonts so every screen looks the same.
 */
public class UITheme {

    public static final Color BG = new Color(11, 18, 36);
    public static final Color PANEL = new Color(20, 32, 56);
    public static final Color CARD = new Color(28, 42, 72);
    public static final Color ACCENT = new Color(56, 132, 255);
    public static final Color ACCENT_DARK = new Color(37, 99, 235);
    public static final Color TEXT = new Color(240, 244, 252);
    public static final Color MUTED = new Color(163, 180, 210);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color SELECTED = new Color(37, 99, 235);
    public static final Color FIELD_BG = new Color(15, 23, 42);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    public static void apply() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Default look and feel is still usable.
        }

        UIManager.put("control", PANEL);
        UIManager.put("nimbusBase", new Color(20, 40, 80));
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("text", TEXT);
        UIManager.put("nimbusLightBackground", FIELD_BG);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", NORMAL);
        UIManager.put("Button.font", BUTTON);
        UIManager.put("TextField.font", NORMAL);
        UIManager.put("PasswordField.font", NORMAL);
        UIManager.put("Table.font", NORMAL);
        UIManager.put("TableHeader.font", BUTTON);
        UIManager.put("ComboBox.font", NORMAL);
        UIManager.put("TextArea.font", NORMAL);
    }

    public static JButton primaryButton(String text) {
        return coloredButton(text, ACCENT, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return coloredButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return coloredButton(text, DANGER, Color.WHITE);
    }

    public static JButton coloredButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(BUTTON);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    public static void styleField(JTextField field) {
        field.setFont(NORMAL);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                new EmptyBorder(8, 10, 8, 10)));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(ACCENT_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(NORMAL);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 23, 42));
        header.setForeground(TEXT);
        header.setFont(BUTTON);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(CARD);
        renderer.setForeground(TEXT);
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, renderer);
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(NORMAL);
        return label;
    }

    public static void paintParent(Component component) {
        component.setBackground(BG);
        component.setForeground(TEXT);
    }
}
