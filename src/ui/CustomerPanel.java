    package ui;

import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private JTable table;

    public CustomerPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton addBtn    = makeButton("+ Add Customer", SUCCESS);
        JButton editBtn   = makeButton("✏ Edit", PRIMARY);
        JButton deleteBtn = makeButton("🗑 Delete", DANGER);
        JButton refreshBtn = makeButton("↻ Refresh", new Color(100, 116, 139));

        topBar.add(refreshBtn);
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);

        table = new JTable();
        styleTable(table);
        loadCustomers();

        refreshBtn.addActionListener(e -> loadCustomers());
        addBtn.addActionListener(e -> showDialog(false, -1));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            showDialog(true, (int) table.getValueAt(row, 0));
        });
        deleteBtn.addActionListener(e -> deleteCustomer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);

        add(topBar, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void loadCustomers() {
        String[] cols = {"ID", "Name", "Phone", "Email", "Address"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT id, name, phone, email, address FROM customers ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2),
                    rs.getString(3), rs.getString(4), rs.getString(5)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setModel(model);
    }

    private void showDialog(boolean isEdit, int customerId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Customer" : "Add Customer", true);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(WHITE);

        JTextField nameTF    = new JTextField();
        JTextField phoneTF   = new JTextField();
        JTextField emailTF   = new JTextField();
        JTextField addressTF = new JTextField();

        if (isEdit) {
            int row = table.getSelectedRow();
            nameTF.setText((String) table.getValueAt(row, 1));
            phoneTF.setText((String) table.getValueAt(row, 2));
            emailTF.setText((String) table.getValueAt(row, 3));
            addressTF.setText((String) table.getValueAt(row, 4));
        }

        form.add(new JLabel("Full Name:"));  form.add(nameTF);
        form.add(new JLabel("Phone:"));      form.add(phoneTF);
        form.add(new JLabel("Email:"));      form.add(emailTF);
        form.add(new JLabel("Address:"));    form.add(addressTF);

        JButton saveBtn = makeButton(isEdit ? "Update" : "Save", SUCCESS);
        saveBtn.addActionListener(e -> {
            String sql = isEdit
                ? "UPDATE customers SET name=?, phone=?, email=?, address=? WHERE id=?"
                : "INSERT INTO customers (name, phone, email, address) VALUES (?,?,?,?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nameTF.getText().trim());
                ps.setString(2, phoneTF.getText().trim());
                ps.setString(3, emailTF.getText().trim());
                ps.setString(4, addressTF.getText().trim());
                if (isEdit) ps.setInt(5, customerId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, isEdit ? "Customer updated!" : "Customer added!");
                loadCustomers();
                dialog.dispose();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(WHITE);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
        int id     = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete customer: " + name + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM customers WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadCustomers();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT_D);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(TEXT_G);
    }
}