package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
import cinema.model.User;
import cinema.util.UITheme;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("CineBook - Login");
        setSize(520, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.PANEL);
        card.setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel title = new JLabel("CineBook", SwingConstants.CENTER);
        title.setFont(UITheme.TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Cinema Ticket Booking System", SwingConstants.CENTER);
        subtitle.setFont(UITheme.NORMAL);
        subtitle.setForeground(UITheme.MUTED);

        JLabel userLabel = UITheme.mutedLabel("Username");
        JLabel passLabel = UITheme.mutedLabel("Password");
        UITheme.styleField(usernameField);
        UITheme.styleField(passwordField);

        JButton loginButton = UITheme.primaryButton("Login");
        JButton registerButton = UITheme.coloredButton("Register", UITheme.CARD, UITheme.TEXT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        card.add(title, gbc);
        gbc.gridy++;
        card.add(subtitle, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(18, 4, 4, 4);
        card.add(userLabel, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 8, 4);
        card.add(usernameField, gbc);
        gbc.gridy++;
        card.add(passLabel, gbc);
        gbc.gridy++;
        card.add(passwordField, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 6, 4);
        card.add(loginButton, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 6, 4);
        card.add(registerButton, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG);
        wrapper.setBorder(new EmptyBorder(40, 50, 40, 50));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });

        // KeyListener: press Enter in the password box to log in
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            if (user.isAdmin()) {
                new AdminDashboard(user).setVisible(true);
            } else {
                new HomeFrame(user).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database.\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
