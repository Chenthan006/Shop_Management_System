package ui;

import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SupplierPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private JTable table;

    public SupplierPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton addBtn    = makeBtn("+ Add Supplier", SUCCESS);
        JButton editBtn   = makeBtn("✏ Edit", PRIMARY);
        JButton deleteBtn = makeBtn("🗑 Delete", DANGER);
        JButton refreshBtn = makeBtn("↻ Refresh", new Color(100, 116, 139));

        topBar.add(refreshBtn); topBar.add(addBtn);
        topBar.add(editBtn); topBar.add(deleteBtn);

        table = new JTable();
        styleTable(table);
        loadData();

        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> showDialog(false, -1));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a supplier first."); return; }
            showDialog(true, (int) table.getValueAt(row, 0));
        });
        deleteBtn.addActionListener(e -> deleteSelected());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        String[] cols = {"ID", "Name", "Phone", "Email", "Address"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT id, name, phone, email, address FROM suppliers ORDER BY name";
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

    private void showDialog(boolean isEdit, int id) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Supplier" : "Add Supplier", true);
        dlg.setSize(400, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(WHITE);

        JTextField nameTF  = new JTextField();
        JTextField phoneTF = new JTextField();
        JTextField emailTF = new JTextField();
        JTextField addrTF  = new JTextField();

        if (isEdit) {
            int row = table.getSelectedRow();
            nameTF.setText((String) table.getValueAt(row, 1));
            phoneTF.setText((String) table.getValueAt(row, 2));
            emailTF.setText((String) table.getValueAt(row, 3));
            addrTF.setText((String) table.getValueAt(row, 4));
        }

        form.add(new JLabel("Name *")); form.add(nameTF);
        form.add(new JLabel("Phone"));  form.add(phoneTF);
        form.add(new JLabel("Email"));  form.add(emailTF);
        form.add(new JLabel("Address")); form.add(addrTF);

        JButton saveBtn = makeBtn(isEdit ? "Update" : "Save", SUCCESS);
        saveBtn.addActionListener(e -> {
            if (nameTF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name is required!");
                return;
            }
            String sql = isEdit
                ? "UPDATE suppliers SET name=?,phone=?,email=?,address=? WHERE id=?"
                : "INSERT INTO suppliers (name,phone,email,address) VALUES(?,?,?,?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nameTF.getText().trim());
                ps.setString(2, phoneTF.getText().trim());
                ps.setString(3, emailTF.getText().trim());
                ps.setString(4, addrTF.getText().trim());
                if (isEdit) ps.setInt(5, id);
                ps.executeUpdate();
                loadData();
                dlg.dispose();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        JPanel btnP = new JPanel();
        btnP.setBackground(WHITE);
        btnP.add(saveBtn);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a supplier first."); return; }
        int id = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        int c = JOptionPane.showConfirmDialog(this, "Delete supplier: " + name + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM suppliers WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate(); loadData();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private JButton makeBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36); t.setShowVerticalLines(false);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT_D); t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(TEXT_G);
    }
}