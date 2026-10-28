package cinema.ui;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.*;

import cinema.util.UITheme;

/**
 * Simulated payment screen — DEMO MODE.
 *
 * Lets the user choose Card / UPI / Cash, enter (fake) details,
 * then shows an animated progress bar "processing".
 * Always succeeds in demo mode.
 */
public class PaymentFrame extends JDialog {

    public enum Result { PAID, CANCELLED }

    private Result outcome = Result.CANCELLED;
    private String transactionId;
    private String paymentMethod;

    private final BigDecimal amount;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck     = new JPanel(cards);

    // Method selection
    private final JToggleButton cardBtn = methodBtn("💳  Debit / Credit Card");
    private final JToggleButton upiBtn  = methodBtn("📱  UPI / PhonePe / GPay");
    private final JToggleButton cashBtn = methodBtn("💵  Pay at Counter");

    // Card fields
    private final JTextField cardNumber  = new JTextField("4111 1111 1111 1111");
    private final JTextField cardExpiry  = new JTextField("12/27");
    private final JTextField cardCvv     = new JTextField("123");
    private final JTextField cardName    = new JTextField("DEMO USER");

    // UPI field
    private final JTextField upiId       = new JTextField("demo@paydemo");

    public PaymentFrame(Frame owner, BigDecimal amount, String customerName) {
        super(owner, "💳 Payment", true);
        this.amount = amount;

        setSize(520, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Secure Payment  —  Rs. " + amount);
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("🔒 DEMO MODE — No real money is charged");
        sub.setFont(UITheme.NORMAL);
        sub.setForeground(UITheme.MUTED);
        JPanel hLeft = new JPanel(new GridLayout(2,1));
        hLeft.setOpaque(false);
        hLeft.add(title); hLeft.add(sub);
        header.add(hLeft, BorderLayout.WEST);

        // Method selector
        JPanel methodPanel = buildMethodSelector();

        // Detail panel (card layout switches between card / UPI / cash forms)
        deck.setBackground(UITheme.BG);
        deck.setBorder(new EmptyBorder(0, 20, 0, 20));
        deck.add(buildCardForm(), "CARD");
        deck.add(buildUpiForm(),  "UPI");
        deck.add(buildCashInfo(), "CASH");

        JButton payBtn    = UITheme.successButton("Pay Now  Rs. " + amount);
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UITheme.PANEL);
        footer.add(cancelBtn);
        footer.add(payBtn);

        add(header, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setBackground(UITheme.BG);
        mid.setBorder(new EmptyBorder(8, 0, 0, 0));
        mid.add(methodPanel, BorderLayout.NORTH);
        mid.add(deck, BorderLayout.CENTER);
        add(mid, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Defaults
        cardBtn.setSelected(true);
        cards.show(deck, "CARD");
        paymentMethod = "Card";

        payBtn.addActionListener(e -> doPay());
        cancelBtn.addActionListener(e -> dispose());
    }

    // ── Method selector ───────────────────────────────────────────────────────

    private JPanel buildMethodSelector() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(cardBtn); bg.add(upiBtn); bg.add(cashBtn);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        p.setBackground(UITheme.BG);
        p.add(cardBtn); p.add(upiBtn); p.add(cashBtn);

        cardBtn.addActionListener(e -> { cards.show(deck, "CARD"); paymentMethod = "Card"; });
        upiBtn.addActionListener(e  -> { cards.show(deck, "UPI");  paymentMethod = "UPI"; });
        cashBtn.addActionListener(e -> { cards.show(deck, "CASH"); paymentMethod = "Cash"; });
        return p;
    }

    private static JToggleButton methodBtn(String label) {
        JToggleButton btn = new JToggleButton(label);
        btn.setFont(UITheme.NORMAL);
        btn.setBackground(UITheme.CARD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(80, 80, 130), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setSelectedIcon(null);
        btn.addChangeListener(e -> {
            btn.setBackground(btn.isSelected() ? new Color(99, 102, 241) : UITheme.CARD);
        });
        return btn;
    }

    // ── Form panels ───────────────────────────────────────────────────────────

    private JPanel buildCardForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(4, 0, 4, 0);

        styleField(cardNumber); styleField(cardExpiry); styleField(cardCvv); styleField(cardName);

        g.gridy = 0; p.add(UITheme.mutedLabel("Card Number"), g);
        g.gridy = 1; p.add(cardNumber, g);
        g.gridy = 2; g.gridwidth = 1;
        p.add(UITheme.mutedLabel("Expiry"), g);
        g.gridx = 1; p.add(UITheme.mutedLabel("CVV"), g);
        g.gridy = 3; g.gridx = 0; p.add(cardExpiry, g);
        g.gridx = 1; p.add(cardCvv, g);
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2;
        p.add(UITheme.mutedLabel("Cardholder Name"), g);
        g.gridy = 5; p.add(cardName, g);
        return p;
    }

    private JPanel buildUpiForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridwidth = 1;
        g.insets = new Insets(4, 0, 4, 0);

        styleField(upiId);
        JLabel hint = UITheme.mutedLabel("Enter your UPI ID  (e.g. name@bankname)");
        g.gridy = 0; p.add(UITheme.mutedLabel("UPI ID"), g);
        g.gridy = 1; p.add(upiId, g);
        g.gridy = 2; p.add(hint, g);
        return p;
    }

    private JPanel buildCashInfo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG);
        JLabel msg = new JLabel(
            "<html><br><center><font size='5'>💵</font><br><br>"
            + "<b style='color:white'>Pay at the counter before your showtime.</b><br><br>"
            + "<span style='color:#999'>Your booking will be held for 30 minutes.<br>"
            + "Please arrive early and show your booking ID.</span></center></html>",
            SwingConstants.CENTER);
        p.add(msg, BorderLayout.CENTER);
        return p;
    }

    // ── Pay action ────────────────────────────────────────────────────────────

    private void doPay() {
        // Show animated processing dialog
        JDialog processing = new JDialog(this, "Processing...", true);
        processing.setSize(300, 140);
        processing.setLocationRelativeTo(this);
        processing.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        processing.getContentPane().setBackground(UITheme.BG);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setString("Processing payment...");
        bar.setForeground(new Color(99, 102, 241));
        JLabel lbl = new JLabel("Please wait…", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(UITheme.NORMAL);
        processing.setLayout(new BorderLayout(8, 8));
        processing.add(lbl, BorderLayout.CENTER);
        processing.add(bar, BorderLayout.SOUTH);
        processing.getRootPane().setBorder(new EmptyBorder(16, 16, 16, 16));

        Timer timer = new Timer(30, null);
        final int[] val = {0};
        timer.addActionListener(te -> {
            val[0] += 2;
            bar.setValue(val[0]);
            if (val[0] >= 100) {
                timer.stop();
                processing.dispose();
            }
        });

        SwingUtilities.invokeLater(() -> {
            timer.start();
            processing.setVisible(true);
            // After processing closes:
            transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase().replace("-", "");
            outcome = Result.PAID;
            dispose();
        });
    }

    private void styleField(JTextField f) { UITheme.styleField(f); }

    public Result getOutcome()       { return outcome; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentMethod() { return paymentMethod; }
}
