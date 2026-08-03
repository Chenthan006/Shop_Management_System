package ui;

import model.ShopSettings;
import model.User;
import util.DBConnection;
import util.PasswordUtil;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class SettingsPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private User currentUser;

    public SettingsPanel(User user) {
        this.currentUser = user;
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Shop settings card
        container.add(buildShopSettingsCard());
        container.add(Box.createVerticalStrut(20));

        // Change password card
        container.add(buildChangePasswordCard());
        container.add(Box.createVerticalStrut(20));

        // About card
        container.add(buildAboutCard());

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Shop Settings Card ──────────────────────────────────────────────
    private JPanel buildShopSettingsCard() {
        JPanel card = makeCard("🏪  Shop Information");

        JPanel form = new JPanel(new GridLayout(0, 2, 16, 14));
        form.setOpaque(false);

        JTextField shopNameTF = makeField(ShopSettings.getShopName());
        JTextField addressTF  = makeField(ShopSettings.getAddress());
        JTextField phoneTF    = makeField(ShopSettings.getPhone());
        JTextField emailTF    = makeField(ShopSettings.getEmail());
        JTextField taxTF      = makeField(String.valueOf(ShopSettings.getTaxPercent()));

        form.add(makeLabel("Shop Name *"));   form.add(shopNameTF);
        form.add(makeLabel("Address"));       form.add(addressTF);
        form.add(makeLabel("Phone"));         form.add(phoneTF);
        form.add(makeLabel("Email"));         form.add(emailTF);
        form.add(makeLabel("Tax % (0 = off)")); form.add(taxTF);

        JButton saveBtn = makeButton("💾  Save Shop Settings", SUCCESS);
        JLabel  statusL = new JLabel(" ");
        statusL.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        saveBtn.addActionListener(e -> {
            String name = shopNameTF.getText().trim();
            if (name.isEmpty()) {
                statusL.setText("❌ Shop name cannot be empty!");
                statusL.setForeground(DANGER);
                return;
            }
            double tax = 0;
            try { tax = Double.parseDouble(taxTF.getText()); } catch (Exception ex) {}

            boolean ok = ShopSettings.save(name,
                addressTF.getText().trim(),
                phoneTF.getText().trim(),
                emailTF.getText().trim(), tax);

            if (ok) {
                statusL.setText("✅ Settings saved successfully!");
                statusL.setForeground(SUCCESS);
            } else {
                statusL.setText("❌ Failed to save. Try again.");
                statusL.setForeground(DANGER);
            }
        });

        card.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bottom.setOpaque(false);
        bottom.add(saveBtn);
        bottom.add(Box.createHorizontalStrut(12));
        bottom.add(statusL);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ── Change Password Card ────────────────────────────────────────────
    private JPanel buildChangePasswordCard() {
        JPanel card = makeCard("🔐  Change Password");

        JPanel form = new JPanel(new GridLayout(0, 2, 16, 14));
        form.setOpaque(false);

        JPasswordField currentPF = makePasswordField();
        JPasswordField newPF     = makePasswordField();
        JPasswordField confirmPF = makePasswordField();

        form.add(makeLabel("Current Password")); form.add(currentPF);
        form.add(makeLabel("New Password"));     form.add(newPF);
        form.add(makeLabel("Confirm Password")); form.add(confirmPF);

        JButton changeBtn = makeButton("🔑  Change Password", PRIMARY);
        JLabel  statusL   = new JLabel(" ");
        statusL.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        changeBtn.addActionListener(e -> {
            String current = new String(currentPF.getPassword());
            String newPass  = new String(newPF.getPassword());
            String confirm  = new String(confirmPF.getPassword());

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                statusL.setText("❌ All fields required!");
                statusL.setForeground(DANGER);
                return;
            }
            if (!newPass.equals(confirm)) {
                statusL.setText("❌ Passwords don't match!");
                statusL.setForeground(DANGER);
                return;
            }
            if (newPass.length() < 6) {
                statusL.setText("❌ Password must be at least 6 characters!");
                statusL.setForeground(DANGER);
                return;
            }

            // Verify current password
            String sql = "SELECT password FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, currentUser.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String stored = rs.getString("password");
                    if (!PasswordUtil.checkPassword(current, stored)) {
                        statusL.setText("❌ Current password is wrong!");
                        statusL.setForeground(DANGER);
                        return;
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }

            boolean ok = User.changePassword(currentUser.getId(), newPass);
            if (ok) {
                statusL.setText("✅ Password changed successfully!");
                statusL.setForeground(SUCCESS);
                currentPF.setText(""); newPF.setText(""); confirmPF.setText("");
            } else {
                statusL.setText("❌ Failed to change password.");
                statusL.setForeground(DANGER);
            }
        });

        card.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bottom.setOpaque(false);
        bottom.add(changeBtn);
        bottom.add(Box.createHorizontalStrut(12));
        bottom.add(statusL);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ── About Card ──────────────────────────────────────────────────────
    private JPanel buildAboutCard() {
        JPanel card = makeCard("ℹ️  About System");

        JPanel info = new JPanel(new GridLayout(0, 2, 16, 10));
        info.setOpaque(false);

        info.add(makeLabel("System Name"));    info.add(makeValueLabel("ShopManager Pro"));
        info.add(makeLabel("Version"));        info.add(makeValueLabel("1.0.0"));
        info.add(makeLabel("Developer"));      info.add(makeValueLabel("Chenthan"));
        info.add(makeLabel("Database"));       info.add(makeValueLabel("MySQL 8.0"));
        info.add(makeLabel("Language"));       info.add(makeValueLabel("Java 17 + Swing"));
        info.add(makeLabel("Logged in as"));   info.add(makeValueLabel(currentUser.getFullName() + " (" + currentUser.getRole() + ")"));

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    // ── Helpers ─────────────────────────────────────────────────────────
    private JPanel makeCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setMaximumSize(new Dimension(900, 400));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(20, 24, 20, 24)
        ));

        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleL.setForeground(TEXT_D);
        titleL.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        titleL.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(0, 0, 14, 0)
        ));
        card.add(titleL, BorderLayout.NORTH);
        return card;
    }

    private JTextField makeField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_D);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return pf;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_G);
        return l;
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_D);
        return l;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }
}