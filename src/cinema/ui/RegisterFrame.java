package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import cinema.dao.UserDAO;
import cinema.util.UITheme;

public class RegisterFrame extends JFrame {

    private final JFrame loginFrame;
    private final UserDAO userDAO = new UserDAO();
    private final JTextField nameField = new JTextField(18);
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);

    public RegisterFrame(JFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("CineBook - Register");
        setSize(520, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.PANEL);
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Create Customer Account", SwingConstants.CENTER);
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);

        UITheme.styleField(nameField);
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);
        UITheme.styleField(confirmField);

        JButton registerButton = UITheme.successButton("Register");
        JButton backButton = UITheme.coloredButton("Back to Login", UITheme.CARD, UITheme.TEXT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridy = 0;
        card.add(title, gbc);
        addLabeledField(card, gbc, "Full name", nameField);
        addLabeledField(card, gbc, "Username", usernameField);
        addLabeledField(card, gbc, "Password", passwordField);
        addLabeledField(card, gbc, "Confirm password", confirmField);
        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 6, 4);
        card.add(registerButton, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 6, 4);
        card.add(backButton, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG);
        wrapper.setBorder(new EmptyBorder(30, 45, 30, 45));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper);

        registerButton.addActionListener(e -> doRegister());
        backButton.addActionListener(e -> goBack());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (loginFrame != null) {
                    loginFrame.setVisible(true);
                }
            }
        });
    }

    private void addLabeledField(JPanel card, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridy++;
        gbc.insets = new Insets(10, 4, 2, 4);
        card.add(UITheme.mutedLabel(label), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(2, 4, 6, 4);
        card.add(field, gbc);
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (username.length() < 4) {
            JOptionPane.showMessageDialog(this, "Username should be at least 4 characters.",
                    "Invalid username", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password should be at least 6 characters.",
                    "Weak password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Check password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this, "This username is already taken. Please choose another.",
                        "Duplicate username", JOptionPane.ERROR_MESSAGE);
                return;
            }
            userDAO.register(name, username, password);
            JOptionPane.showMessageDialog(this, "Registration successful. You can now log in.",
                    "Welcome to CineBook", JOptionPane.INFORMATION_MESSAGE);
            goBack();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not register: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() {
        dispose();
        if (loginFrame != null) {
            loginFrame.setVisible(true);
        }
    }
}
