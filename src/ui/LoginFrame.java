package ui;

import model.User;
import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class LoginFrame extends JFrame {

    // Colors
    private static final Color PRIMARY   = new Color(30, 58, 138);   // Deep blue
    private static final Color ACCENT    = new Color(59, 130, 246);   // Bright blue
    private static final Color BG_WHITE  = new Color(248, 250, 252);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color BORDER_C  = new Color(203, 213, 225);
    private static final Color ERROR_C   = new Color(220, 38, 38);

    private JTextField     usernameTF;
    private JPasswordField passwordTF;
    private JLabel         errorLabel;

    public LoginFrame() {
        super("Shop Management System — Login");
        setSize(820, 500);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left panel (brand)
        JPanel leftPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), new Color(17, 24, 80));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(310, 500));
        leftPanel.setLayout(new GridBagLayout());

        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🛒");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleL = new JLabel("ShopManager");
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleL.setForeground(Color.WHITE);
        titleL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subL = new JLabel("Pro Edition");
        subL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subL.setForeground(new Color(147, 197, 253));
        subL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descL = new JLabel("<html><center><font color='#93C5FD' size='3'>" +
                "Complete shop management<br>system with real-time<br>inventory tracking</font></center></html>");
        descL.setAlignmentX(Component.CENTER_ALIGNMENT);
        descL.setBorder(new EmptyBorder(20, 20, 0, 20));

        brandBox.add(icon);
        brandBox.add(Box.createVerticalStrut(10));
        brandBox.add(titleL);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(subL);
        brandBox.add(Box.createVerticalStrut(20));
        brandBox.add(descL);
        leftPanel.add(brandBox);

        // Right panel (form)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(BG_WHITE);
        rightPanel.setLayout(new GridBagLayout());

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setMaximumSize(new Dimension(320, 400));

        JLabel welcomeL = new JLabel("Welcome back");
        welcomeL.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeL.setForeground(TEXT_DARK);
        welcomeL.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleL = new JLabel("Sign in to your account");
        subtitleL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleL.setForeground(TEXT_GRAY);
        subtitleL.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(welcomeL);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(subtitleL);
        formBox.add(Box.createVerticalStrut(28));

        // Username field
        formBox.add(makeLabel("Username"));
        formBox.add(Box.createVerticalStrut(6));
        usernameTF = makeTextField("Enter your username");
        formBox.add(usernameTF);
        formBox.add(Box.createVerticalStrut(16));

        // Password field
        formBox.add(makeLabel("Password"));
        formBox.add(Box.createVerticalStrut(6));
        passwordTF = makePasswordField("Enter your password");
        formBox.add(passwordTF);
        formBox.add(Box.createVerticalStrut(8));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_C);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(errorLabel);
        formBox.add(Box.createVerticalStrut(18));

        // Login button
        JButton loginBtn = new JButton("Sign In") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY : ACCENT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(320, 44));
        loginBtn.setMaximumSize(new Dimension(320, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(loginBtn);

        rightPanel.add(formBox);

        // Actions
        loginBtn.addActionListener(e -> doLogin());
        usernameTF.addActionListener(e -> passwordTF.requestFocus());
        passwordTF.addActionListener(e -> doLogin());

        add(leftPanel,  BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setPreferredSize(new Dimension(320, 44));
        tf.setMaximumSize(new Dimension(320, 44));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JPasswordField makePasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField(20);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setForeground(TEXT_DARK);
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        pf.setPreferredSize(new Dimension(320, 44));
        pf.setMaximumSize(new Dimension(320, 44));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private void doLogin() {
        // FIXED: Use correct field names (usernameTF and passwordTF, not usernameField and passwordField)
        String username = usernameTF.getText();
        String password = new String(passwordTF.getPassword());
        
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        
        // Test database connection first
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("DB Connection: " + (conn != null ? "OK" : "FAILED"));
            System.out.println("Database: " + conn.getCatalog());
            
            // Check if users exist
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("Total users in DB: " + rs.getInt(1));
            }
            
        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        User user = User.login(username, password);
        
        if (user != null) {
            System.out.println("Login SUCCESS!");
            new MainFrame(user).setVisible(true);
            dispose();
        } else {
            System.out.println("Login FAILED!");
            errorLabel.setText("Invalid username or password");
            errorLabel.setVisible(true);
        }
    }

    public static void main(String[] args) {
        // Set modern Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* use default */ }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}