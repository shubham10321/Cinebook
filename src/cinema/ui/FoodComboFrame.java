package cinema.ui;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import cinema.model.FoodCombo;
import cinema.util.UITheme;

/**
 * Food combo selection step shown between seat selection and payment.
 * Returns a map of comboId → quantity when the user clicks "Continue".
 */
public class FoodComboFrame extends JDialog {

    private final java.util.List<FoodCombo> combos;
    /** comboId → quantity chosen (only combos with qty > 0) */
    private Map<Integer, Integer> result = null;

    private BigDecimal baseTotal;
    private final JLabel totalLabel = new JLabel();
    private final java.util.List<JSpinner> spinners = new ArrayList<>();

    public FoodComboFrame(Frame owner, java.util.List<FoodCombo> combos, BigDecimal ticketTotal) {
        super(owner, "🍿 Add Food & Drinks", true);
        this.combos = combos;
        this.baseTotal = ticketTotal;

        setSize(560, 600);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 10));

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Add Food & Drinks to your booking");
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Optional — quantities below add to your total");
        sub.setFont(UITheme.NORMAL);
        sub.setForeground(UITheme.MUTED);
        JPanel hLeft = new JPanel(new GridLayout(2,1));
        hLeft.setOpaque(false);
        hLeft.add(title); hLeft.add(sub);
        header.add(hLeft, BorderLayout.WEST);

        // ── Combo list ─────────────────────────────────────────────────────────
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UITheme.BG);
        listPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        for (FoodCombo combo : combos) {
            listPanel.add(buildComboRow(combo));
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getViewport().setBackground(UITheme.BG);
        scroll.setBorder(null);

        // ── Footer ─────────────────────────────────────────────────────────────
        totalLabel.setFont(UITheme.HEADING);
        totalLabel.setForeground(Color.WHITE);
        updateTotal();

        JButton skipBtn = UITheme.coloredButton("Skip (no food)", UITheme.CARD, UITheme.TEXT);
        JButton continueBtn = UITheme.successButton("Continue to Payment →");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.PANEL);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        footer.add(totalLabel, BorderLayout.WEST);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(skipBtn);
        btnRow.add(continueBtn);
        footer.add(btnRow, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        skipBtn.addActionListener(e -> { result = Collections.emptyMap(); dispose(); });
        continueBtn.addActionListener(e -> {
            result = collectSelection();
            dispose();
        });
    }

    // ── Build one combo row ────────────────────────────────────────────────────

    private JPanel buildComboRow(FoodCombo combo) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(UITheme.CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(50, 50, 80), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Left: emoji + name + description
        JLabel emojiLabel = new JLabel(combo.getEmoji());
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel name = new JLabel(combo.getName());
        name.setFont(UITheme.BUTTON);
        name.setForeground(Color.WHITE);
        JLabel desc = new JLabel(combo.getDescription());
        desc.setFont(UITheme.NORMAL);
        desc.setForeground(UITheme.MUTED);
        info.add(name);
        info.add(desc);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(emojiLabel);
        left.add(info);

        // Right: price + spinner
        JLabel price = new JLabel("Rs. " + combo.getPrice());
        price.setFont(UITheme.BUTTON);
        price.setForeground(UITheme.SUCCESS);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(60, 28));
        spinner.putClientProperty("comboId", combo.getComboId());
        spinner.putClientProperty("comboPrice", combo.getPrice());
        spinner.addChangeListener(e2 -> updateTotal());
        spinners.add(spinner);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(price);
        right.add(spinner);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ── Total calculation ──────────────────────────────────────────────────────

    private void updateTotal() {
        BigDecimal food = BigDecimal.ZERO;
        for (JSpinner s : spinners) {
            int qty = (int) s.getValue();
            BigDecimal unitPrice = (BigDecimal) s.getClientProperty("comboPrice");
            if (qty > 0 && unitPrice != null) {
                food = food.add(unitPrice.multiply(new BigDecimal(qty)));
            }
        }
        totalLabel.setText("Grand Total: Rs. " + baseTotal.add(food));
    }

    private Map<Integer, Integer> collectSelection() {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (JSpinner s : spinners) {
            int qty = (int) s.getValue();
            if (qty > 0) {
                int comboId = (int) s.getClientProperty("comboId");
                map.put(comboId, qty);
            }
        }
        return map;
    }

    /** Show dialog and return combo selection (empty map if skipped). */
    public Map<Integer, Integer> show(Component parent) {
        setVisible(true);
        return result == null ? Collections.emptyMap() : result;
    }

    public java.util.List<FoodCombo> getCombos() { return combos; }
}
