package cinema.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.dao.UserDAO;
import cinema.util.UITheme;

/**
 * Forgot-password flow (DEMO mode):
 *   Step 1 — Enter username or email  →  OTP is generated & shown on screen
 *   Step 2 — Enter OTP + new password →  Password reset, back to login
 */
public class ForgotPasswordFrame extends JFrame {

    private final JFrame parentLogin;
    private final UserDAO userDAO = new UserDAO();

    // Step-1 widgets
    private final JTextField identifierField = new JTextField(18);

    // Step-2 widgets
    private final JTextField otpField        = new JTextField(18);
    private final JPasswordField newPassField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);

    private String currentIdentifier;   // username / email from step 1
    private String expectedOtp;         // OTP shown on screen

    private final CardLayout cards = new CardLayout();
    private final JPanel deck     = new JPanel(cards);

    public ForgotPasswordFrame(JFrame parentLogin) {
        this.parentLogin = parentLogin;
        setTitle("CineBook – Forgot Password");
        setSize(500, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        deck.setBackground(UITheme.BG);
        deck.add(buildStep1(), "STEP1");
        deck.add(buildStep2(), "STEP2");
        cards.show(deck, "STEP1");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG);
        wrapper.setBorder(new EmptyBorder(30, 40, 30, 40));
        wrapper.add(deck, BorderLayout.CENTER);
        add(wrapper);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (parentLogin != null) parentLogin.setVisible(true);
            }
        });
    }

    // ── Step 1: Enter username / email ─────────────────────────────────────────

    private JPanel buildStep1() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.PANEL);
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Forgot Password", SwingConstants.CENTER);
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Enter your username or registered email", SwingConstants.CENTER);
        sub.setFont(UITheme.NORMAL);
        sub.setForeground(UITheme.MUTED);

        UITheme.styleField(identifierField);
        JButton sendBtn = UITheme.primaryButton("Send OTP");
        JButton backBtn = UITheme.coloredButton("Back to Login", UITheme.CARD, UITheme.TEXT);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        g.gridy = 0; card.add(title, g);
        g.gridy = 1; g.insets = new Insets(4, 4, 14, 4); card.add(sub, g);
        g.gridy = 2; g.insets = new Insets(10, 4, 2, 4); card.add(UITheme.mutedLabel("Username or Email"), g);
        g.gridy = 3; g.insets = new Insets(2, 4, 12, 4); card.add(identifierField, g);
        g.gridy = 4; g.insets = new Insets(16, 4, 6, 4); card.add(sendBtn, g);
        g.gridy = 5; g.insets = new Insets(4, 4, 6, 4);  card.add(backBtn, g);

        sendBtn.addActionListener(e -> doSendOtp());
        backBtn.addActionListener(e -> { dispose(); if (parentLogin != null) parentLogin.setVisible(true); });
        return card;
    }

    // ── Step 2: Enter OTP + new password ──────────────────────────────────────

    private JPanel buildStep2() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.PANEL);
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Reset Password", SwingConstants.CENTER);
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);

        UITheme.styleField(otpField);
        UITheme.styleField(newPassField);
        UITheme.styleField(confirmField);

        JButton resetBtn = UITheme.successButton("Reset Password");

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        g.gridy = 0; card.add(title, g);
        addField(card, g, "OTP Code",     otpField);
        addField(card, g, "New Password", newPassField);
        addField(card, g, "Confirm Password", confirmField);
        g.gridy += 2; g.insets = new Insets(16, 4, 6, 4);
        card.add(resetBtn, g);

        resetBtn.addActionListener(e -> doReset());
        return card;
    }

    private void addField(JPanel card, GridBagConstraints g, String label, JTextField field) {
        g.gridy++; g.insets = new Insets(10, 4, 2, 4); card.add(UITheme.mutedLabel(label), g);
        g.gridy++; g.insets = new Insets(2, 4, 6, 4);  card.add(field, g);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doSendOtp() {
        String id = identifierField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username or email.");
            return;
        }
        try {
            String otp = userDAO.generateAndSaveOtp(id);
            if (otp == null) {
                JOptionPane.showMessageDialog(this,
                    "No account found with that username or email.",
                    "Not found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            currentIdentifier = id;
            expectedOtp = otp;

            // DEMO: show OTP on screen
            JOptionPane.showMessageDialog(this,
                "<html><b>📱 DEMO MODE — OTP Delivery</b><br><br>"
                + "In a real app this would be sent to your email / phone.<br><br>"
                + "<center><font size='5' color='blue'><b>" + otp + "</b></font></center><br>"
                + "This OTP expires in 10 minutes.</html>",
                "Your OTP", JOptionPane.INFORMATION_MESSAGE);

            cards.show(deck, "STEP2");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doReset() {
        String otp     = otpField.getText().trim();
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (otp.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
            return;
        }
        try {
            if (!userDAO.verifyOtp(currentIdentifier, otp)) {
                JOptionPane.showMessageDialog(this,
                    "Invalid or expired OTP. Please go back and try again.",
                    "OTP Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            userDAO.resetPassword(currentIdentifier, newPass);
            JOptionPane.showMessageDialog(this,
                "Password reset successfully! You can now log in with your new password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (parentLogin != null) parentLogin.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
