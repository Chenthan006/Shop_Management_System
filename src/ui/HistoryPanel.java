package ui;

import model.Sale;
import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HistoryPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_G  = new Color(100, 116, 139);
    private static final Color TEXT_D  = new Color(15, 23, 42);

    private JTable table;
    private JTextField fromDateTF, toDateTF, searchTF;

    public HistoryPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Date from
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fromDateTF = makeTextField("2026-01-01", 100);

        // Date to
        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toDateTF = makeTextField("2026-12-31", 100);

        // Search
        JLabel searchLabel = new JLabel("Invoice:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTF = makeTextField("Search...", 120);

        // Buttons
        JButton filterBtn   = makeButton("🔍 Filter", PRIMARY);
        JButton refreshBtn  = makeButton("↻ All", new Color(100, 116, 139));

        filterBtn.addActionListener(e -> filterSales());
        refreshBtn.addActionListener(e -> {
            fromDateTF.setText("2026-01-01");
            toDateTF.setText("2026-12-31");
            searchTF.setText("");
            table.setModel(Sale.getSalesHistory());
        });

        filterBar.add(fromLabel);
        filterBar.add(fromDateTF);
        filterBar.add(toLabel);
        filterBar.add(toDateTF);
        filterBar.add(searchLabel);
        filterBar.add(searchTF);
        filterBar.add(filterBtn);
        filterBar.add(refreshBtn);

        // Table
        table = new JTable(Sale.getSalesHistory());
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);

        // Summary bar at bottom
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        summaryBar.setBackground(new Color(239, 246, 255));
        summaryBar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JLabel totalSalesLabel = new JLabel("Total Transactions: —");
        JLabel totalRevenueLabel = new JLabel("Total Revenue: —");
        totalSalesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalSalesLabel.setForeground(PRIMARY);
        totalRevenueLabel.setForeground(new Color(16, 185, 129));

        summaryBar.add(totalSalesLabel);
        summaryBar.add(new JSeparator(JSeparator.VERTICAL));
        summaryBar.add(totalRevenueLabel);

        // Update summary when table changes
        table.getModel().addTableModelListener(e -> {
            int rows = table.getRowCount();
            double revenue = 0;
            for (int i = 0; i < rows; i++) {
                try {
                    revenue += Double.parseDouble(table.getValueAt(i, 3).toString());
                } catch (Exception ex) {}
            }
            totalSalesLabel.setText("Total Transactions: " + rows);
            totalRevenueLabel.setText("Total Revenue: Rs. " + String.format("%.2f", revenue));
        });

        add(filterBar, BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(summaryBar, BorderLayout.SOUTH);
    }

    private void filterSales() {
        String from   = fromDateTF.getText().trim();
        String to     = toDateTF.getText().trim();
        String search = searchTF.getText().trim();

        String[] cols = {"Invoice", "Customer", "Cashier", "Total (Rs.)", "Payment", "Date & Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        String sql = "SELECT s.invoice_number, c.name, u.full_name, s.total, s.payment_method, s.sale_date " +
                     "FROM sales s " +
                     "JOIN customers c ON s.customer_id = c.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                     (search.isEmpty() ? "" : "AND s.invoice_number LIKE ? ") +
                     "ORDER BY s.sale_date DESC LIMIT 500";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            if (!search.isEmpty()) ps.setString(3, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    String.format("%.2f", rs.getDouble(4)),
                    rs.getString(5), rs.getTimestamp(6).toString()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        table.setModel(model);
    }

    private JTextField makeTextField(String text, int width) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(width, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(2, 8, 2, 8)));
        return tf;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
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
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
    }
}