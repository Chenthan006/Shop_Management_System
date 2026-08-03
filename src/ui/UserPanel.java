package ui;

import model.User;
import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private JTable table;

    public UserPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton addBtn     = makeButton("+ Add User", SUCCESS);
        JButton deleteBtn  = makeButton("🗑 Delete", DANGER);
        JButton refreshBtn = makeButton("↻ Refresh", new Color(100, 116, 139));

        topBar.add(refreshBtn);
        topBar.add(addBtn);
        topBar.add(deleteBtn);

        table = new JTable();
        styleTable(table);
        loadUsers();

        refreshBtn.addActionListener(e -> loadUsers());
        addBtn.addActionListener(e -> showAddDialog());
        deleteBtn.addActionListener(e -> deleteUser());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(WHITE);

        add(topBar, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void loadUsers() {
        String[] cols = {"ID", "Username", "Full Name", "Role", "Email", "Phone", "Created"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT id, username, full_name, role, email, phone, created_at FROM users ORDER BY role, full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6),
                    rs.getTimestamp(7).toString().substring(0, 10)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        table.setModel(model);
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(WHITE);

        JTextField usernameTF = new JTextField();
        JPasswordField passTF = new JPasswordField();
        JTextField fullNameTF = new JTextField();
        JTextField emailTF    = new JTextField();
        JTextField phoneTF    = new JTextField();
        JComboBox<String> roleCB = new JComboBox<>(new String[]{"CASHIER", "ADMIN"});

        form.add(new JLabel("Username:"));  form.add(usernameTF);
        form.add(new JLabel("Password:"));  form.add(passTF);
        form.add(new JLabel("Full Name:")); form.add(fullNameTF);
        form.add(new JLabel("Role:"));      form.add(roleCB);
        form.add(new JLabel("Email:"));     form.add(emailTF);
        form.add(new JLabel("Phone:"));     form.add(phoneTF);

        JButton saveBtn = makeButton("Create User", SUCCESS);
        saveBtn.addActionListener(e -> {
            String username = usernameTF.getText().trim();
            String password = new String(passTF.getPassword());
            String fullName = fullNameTF.getText().trim();
            String role     = (String) roleCB.getSelectedItem();
            String email    = emailTF.getText().trim();
            String phone    = phoneTF.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username, password and full name are required.");
                return;
            }
            boolean ok = User.addUser(username, password, role, fullName, email, phone);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "User created successfully!");
                loadUsers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed! Username may already exist.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(WHITE);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        int id       = (int) table.getValueAt(row, 0);
        String uname = (String) table.getValueAt(row, 1);
        if (uname.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Cannot delete the main admin account!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user: " + uname + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadUsers();
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