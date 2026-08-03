package ui;

import model.Product;
import model.Sale;
import model.User;
import util.InvoicePrinter;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color PURPLE  = new Color(139, 92, 246);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private User currentUser;
    private JTable productTable, cartTable;
    private DefaultTableModel cartModel;
    private JTextField searchTF, discountTF;
    private JLabel subtotalLabel, totalLabel, invoiceLabel;
    private JComboBox<String> paymentCB;

    private List<int[]> cartItems   = new ArrayList<>();
    private List<Double> cartPrices = new ArrayList<>();
    private List<String> cartNames  = new ArrayList<>();
    private double subtotal = 0;

    private String lastInvoiceNumber = "";
    private double lastTotal = 0, lastDiscount = 0, lastSubtotal = 0;
    private List<String[]> lastCartRows = new ArrayList<>();

    public POSPanel(User user) {
        this.currentUser = user;
        setBackground(BG);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildLeftPanel(),  BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setBackground(WHITE);
        searchBox.setBorder(new LineBorder(BORDER, 1, true));
        JLabel icon = new JLabel("  🔍 ");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        searchTF = new JTextField();
        searchTF.setBorder(null);
        searchTF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTF.setBackground(WHITE);
        searchBox.add(icon, BorderLayout.WEST);
        searchBox.add(searchTF, BorderLayout.CENTER);

        searchTF.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String kw = searchTF.getText().trim();
                productTable.setModel(kw.isEmpty() ? Product.getAllProducts() : Product.searchProducts(kw));
            }
        });

        searchRow.add(searchBox, BorderLayout.CENTER);
        JButton addBtn = makeButton("Add to Cart →", PRIMARY);
        addBtn.setPreferredSize(new Dimension(130, 38));
        addBtn.addActionListener(e -> addToCart());
        searchRow.add(addBtn, BorderLayout.EAST);
        panel.add(searchRow, BorderLayout.NORTH);

        productTable = new JTable(Product.getAllProducts());
        styleTable(productTable);
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addToCart();
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Double-click or select + 'Add to Cart'");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(TEXT_G);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(360, 600));

        invoiceLabel = new JLabel("Invoice: " + Sale.generateInvoiceNumber());
        invoiceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        invoiceLabel.setForeground(PRIMARY);
        panel.add(invoiceLabel, BorderLayout.NORTH);

        String[] cols = {"Product", "Qty", "Price", "Total"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        styleTable(cartTable);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(new LineBorder(BORDER, 1, true));
        cartScroll.getViewport().setBackground(WHITE);
        cartScroll.setPreferredSize(new Dimension(360, 220));
        panel.add(cartScroll, BorderLayout.CENTER);

        // Billing
        JPanel billingPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        billingPanel.setBackground(WHITE);
        billingPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(12, 12, 12, 12)));

        subtotalLabel = new JLabel("Rs. 0.00");
        totalLabel    = new JLabel("Rs. 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(SUCCESS);

        discountTF = new JTextField("0");
        discountTF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        discountTF.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { updateTotals(); }
        });

        paymentCB = new JComboBox<>(new String[]{"CASH", "CARD"});
        paymentCB.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        billingPanel.add(makeLabel("Subtotal:"));       billingPanel.add(subtotalLabel);
        billingPanel.add(makeLabel("Discount (Rs.):")); billingPanel.add(discountTF);
        billingPanel.add(makeLabel("TOTAL:"));          billingPanel.add(totalLabel);
        billingPanel.add(makeLabel("Payment:"));        billingPanel.add(paymentCB);

        JButton removeBtn   = makeButton("✕ Remove", DANGER);
        JButton clearBtn    = makeButton("Clear", new Color(100, 116, 139));
        JButton printBtn    = makeButton("🖨 Print Last Invoice", PURPLE);
        JButton checkoutBtn = makeButton("✓  CHECKOUT", SUCCESS);
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        removeBtn.addActionListener(e -> removeFromCart());
        clearBtn.addActionListener(e -> clearCart());
        printBtn.addActionListener(e -> printLastInvoice());
        checkoutBtn.addActionListener(e -> checkout());

        JPanel actionPanel = new JPanel(new BorderLayout(0, 6));
        actionPanel.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 6, 0));
        row1.setOpaque(false);
        row1.add(removeBtn); row1.add(clearBtn);

        actionPanel.add(billingPanel, BorderLayout.NORTH);
        actionPanel.add(row1, BorderLayout.CENTER);

        JPanel row2 = new JPanel(new GridLayout(2, 1, 0, 6));
        row2.setOpaque(false);
        row2.add(printBtn);
        row2.add(checkoutBtn);
        actionPanel.add(row2, BorderLayout.SOUTH);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product first."); return; }

        int productId = (int) productTable.getValueAt(row, 0);
        String name   = (String) productTable.getValueAt(row, 1);
        int stock     = (int) productTable.getValueAt(row, 4);
        double price  = Double.parseDouble(productTable.getValueAt(row, 3).toString());

        if (stock <= 0) { JOptionPane.showMessageDialog(this, "Out of stock!"); return; }

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity for: " + name, "1");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0 || qty > stock) {
                JOptionPane.showMessageDialog(this, "Invalid quantity! Max: " + stock);
                return;
            }

            for (int i = 0; i < cartItems.size(); i++) {
                if (cartItems.get(i)[0] == productId) {
                    cartItems.get(i)[1] += qty;
                    cartModel.setValueAt(cartItems.get(i)[1], i, 1);
                    cartModel.setValueAt(String.format("%.2f", cartPrices.get(i) * cartItems.get(i)[1]), i, 3);
                    updateTotals();
                    return;
                }
            }

            cartItems.add(new int[]{productId, qty});
            cartPrices.add(price);
            cartNames.add(name);
            cartModel.addRow(new Object[]{name, qty,
                String.format("%.2f", price), String.format("%.2f", price * qty)});
            updateTotals();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number.");
        }
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select item to remove."); return; }
        cartItems.remove(row); cartPrices.remove(row); cartNames.remove(row);
        cartModel.removeRow(row);
        updateTotals();
    }

    private void clearCart() {
        cartItems.clear(); cartPrices.clear(); cartNames.clear();
        while (cartModel.getRowCount() > 0) cartModel.removeRow(0);
        updateTotals();
    }

    private void updateTotals() {
        subtotal = 0;
        for (int i = 0; i < cartItems.size(); i++)
            subtotal += cartPrices.get(i) * cartItems.get(i)[1];
        double discount = 0;
        try { discount = Double.parseDouble(discountTF.getText()); } catch (Exception e) {}
        double total = Math.max(0, subtotal - discount);
        subtotalLabel.setText(String.format("Rs. %.2f", subtotal));
        totalLabel.setText(String.format("Rs. %.2f", total));
    }

    private void checkout() {
        if (cartItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty!"); return; }

        double discount = 0;
        try { discount = Double.parseDouble(discountTF.getText()); } catch (Exception e) {}
        double total   = Math.max(0, subtotal - discount);
        String payment = (String) paymentCB.getSelectedItem();
        double[] prices = cartPrices.stream().mapToDouble(Double::doubleValue).toArray();

        boolean ok = Sale.saveSale(1, currentUser.getId(), cartItems, prices, subtotal, discount, total, payment);

        if (ok) {
            lastInvoiceNumber = invoiceLabel.getText().replace("Invoice: ", "");
            lastTotal = total; lastDiscount = discount; lastSubtotal = subtotal;

            // Save cart rows for printing
            lastCartRows.clear();
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                lastCartRows.add(new String[]{
                    cartModel.getValueAt(i, 0).toString(),
                    cartModel.getValueAt(i, 1).toString(),
                    cartModel.getValueAt(i, 2).toString(),
                    cartModel.getValueAt(i, 3).toString()
                });
            }

            int choice = JOptionPane.showOptionDialog(this,
                "✅ Sale completed!\nInvoice: " + lastInvoiceNumber + "\nTotal: Rs. " + String.format("%.2f", total),
                "Sale Successful", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"🖨 Print Invoice", "Close"}, "🖨 Print Invoice");

            if (choice == 0) printLastInvoice();

            clearCart();
            invoiceLabel.setText("Invoice: " + Sale.generateInvoiceNumber());
            productTable.setModel(Product.getAllProducts());
        } else {
            JOptionPane.showMessageDialog(this, "Sale failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printLastInvoice() {
        if (lastInvoiceNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No recent sale! Complete a checkout first.");
            return;
        }
        InvoicePrinter.printInvoice(lastInvoiceNumber, currentUser.getFullName(),
            (String) paymentCB.getSelectedItem(),
            lastCartRows, lastSubtotal, lastDiscount, lastTotal);
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36); t.setShowVerticalLines(false);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT_D); t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(TEXT_G);
    }
}