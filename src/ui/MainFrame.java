package ui;

import model.Sale;
import model.ShopSettings;
import model.User;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BG  = new Color(15, 23, 42);
    private static final Color SIDEBAR_HOV = new Color(30, 41, 59);
    private static final Color SIDEBAR_SEL = new Color(37, 99, 235);
    private static final Color SIDEBAR_SEC = new Color(20, 30, 55);
    private static final Color CONTENT_BG  = new Color(243, 244, 246);
    private static final Color TEXT_WHITE  = Color.WHITE;
    private static final Color TEXT_MUTED  = new Color(148, 163, 184);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color BORDER_C    = new Color(226, 232, 240);
    private static final Color ACCENT      = new Color(59, 130, 246);

    private User currentUser;
    private JPanel contentArea;
    private JLabel pageTitleLabel;
    private JLabel sidebarShopName;
    private JButton activeBtn = null;

    public MainFrame(User user) {
        this.currentUser = user;
        ShopSettings.load();
        setTitle(ShopSettings.getShopName() + " — " + user.getFullName());
        setSize(1280, 720);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebarWrapper(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(CONTENT_BG);
        rightPanel.add(buildTopBar(), BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(CONTENT_BG);
        rightPanel.add(contentArea, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
        showDashboard();
    }

    private JScrollPane buildSidebarWrapper() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 16));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(220, 68));
        JLabel logoIcon = new JLabel("🛒");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JPanel logoTextPanel = new JPanel();
        logoTextPanel.setOpaque(false);
        logoTextPanel.setLayout(new BoxLayout(logoTextPanel, BoxLayout.Y_AXIS));
        sidebarShopName = new JLabel(ShopSettings.getShopName());
        sidebarShopName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sidebarShopName.setForeground(TEXT_WHITE);
        JLabel proLabel = new JLabel("Management System");
        proLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        proLabel.setForeground(TEXT_MUTED);
        logoTextPanel.add(sidebarShopName);
        logoTextPanel.add(proLabel);
        logoPanel.add(logoIcon);
        logoPanel.add(logoTextPanel);
        sidebar.add(logoPanel);

        JPanel accentLine = new JPanel();
        accentLine.setBackground(ACCENT);
        accentLine.setMaximumSize(new Dimension(220, 2));
        sidebar.add(accentLine);
        sidebar.add(Box.createVerticalStrut(10));

        // Main nav
        sidebar.add(makeSectionLabel("MAIN"));
        JButton btnDash     = makeSidebarBtn("📊", "Dashboard");
        JButton btnProducts = makeSidebarBtn("📦", "Products");
        JButton btnPOS      = makeSidebarBtn("🛒", "Point of Sale");
        JButton btnHistory  = makeSidebarBtn("📋", "Sales History");
        JButton btnCustomer = makeSidebarBtn("👥", "Customers");
        JButton btnSupplier = makeSidebarBtn("🏭", "Suppliers");

        btnDash.addActionListener(e     -> { showDashboard(); setActive(btnDash); });
        btnProducts.addActionListener(e -> { showProducts();  setActive(btnProducts); });
        btnPOS.addActionListener(e      -> { showPOS();       setActive(btnPOS); });
        btnHistory.addActionListener(e  -> { showHistory();   setActive(btnHistory); });
        btnCustomer.addActionListener(e -> { showCustomers(); setActive(btnCustomer); });
        btnSupplier.addActionListener(e -> { showSuppliers(); setActive(btnSupplier); });

        sidebar.add(btnDash); sidebar.add(btnProducts); sidebar.add(btnPOS);
        sidebar.add(btnHistory); sidebar.add(btnCustomer); sidebar.add(btnSupplier);

        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(makeSectionLabel("REPORTS"));
        JButton btnReports = makeSidebarBtn("📈", "Sales Report");
        btnReports.addActionListener(e -> { showReports(); setActive(btnReports); });
        sidebar.add(btnReports);

        if (currentUser.isAdmin()) {
            sidebar.add(Box.createVerticalStrut(6));
            sidebar.add(makeSectionLabel("ADMIN"));
            JButton btnUsers    = makeSidebarBtn("👤", "Manage Users");
            JButton btnSettings = makeSidebarBtn("⚙️", "Settings");
            btnUsers.addActionListener(e    -> { showUsers();    setActive(btnUsers); });
            btnSettings.addActionListener(e -> { showSettings(); setActive(btnSettings); });
            sidebar.add(btnUsers);
            sidebar.add(btnSettings);
        }

        sidebar.add(Box.createVerticalGlue());

        // Low stock alert
        int ls = Sale.getLowStockCount();
        if (ls > 0) {
            JPanel alertBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 7));
            alertBox.setBackground(new Color(69, 26, 3));
            alertBox.setMaximumSize(new Dimension(220, 38));
            JLabel al = new JLabel("⚠  " + ls + " low stock item" + (ls > 1 ? "s" : ""));
            al.setFont(new Font("Segoe UI", Font.BOLD, 11));
            al.setForeground(new Color(252, 165, 0));
            alertBox.add(al);
            sidebar.add(alertBox);
        }

        // User profile
        JPanel profileCard = new JPanel(new BorderLayout());
        profileCard.setBackground(SIDEBAR_SEC);
        profileCard.setMaximumSize(new Dimension(220, 58));
        profileCard.setBorder(new EmptyBorder(9, 14, 9, 14));
        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pLeft.setOpaque(false);
        JLabel ava = new JLabel(currentUser.isAdmin() ? "👑" : "👤");
        ava.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JPanel np = new JPanel(); np.setOpaque(false);
        np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
        JLabel nl = new JLabel(currentUser.getFullName());
        nl.setFont(new Font("Segoe UI", Font.BOLD, 11)); nl.setForeground(TEXT_WHITE);
        JLabel rl = new JLabel(currentUser.getRole());
        rl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        rl.setForeground(currentUser.isAdmin() ? new Color(250,204,21) : TEXT_MUTED);
        np.add(nl); np.add(rl);
        pLeft.add(ava); pLeft.add(np);
        profileCard.add(pLeft, BorderLayout.CENTER);
        JButton logoutBtn = new JButton("⏻");
        logoutBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        logoutBtn.setForeground(new Color(239, 68, 68));
        logoutBtn.setBackground(SIDEBAR_SEC);
        logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) { dispose(); new LoginFrame().setVisible(true); }
        });
        profileCard.add(logoutBtn, BorderLayout.EAST);
        sidebar.add(profileCard);

        JScrollPane scroll = new JScrollPane(sidebar);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        scroll.setPreferredSize(new Dimension(220, 720));
        return scroll;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,0,1,0,BORDER_C), new EmptyBorder(13,24,13,24)));
        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitleLabel.setForeground(TEXT_DARK);
        JPanel rp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rp.setOpaque(false);
        int ls = Sale.getLowStockCount();
        JLabel bell = new JLabel(ls > 0 ? "🔔 " + ls : "🔔");
        bell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        bell.setForeground(ls > 0 ? new Color(239,68,68) : new Color(156,163,175));
        bell.setToolTipText(ls > 0 ? ls + " low stock alerts" : "No alerts");
        JLabel badge = new JLabel("  " + currentUser.getRole() + "  ") {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentUser.isAdmin() ? new Color(254,243,199) : new Color(219,234,254));
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),12,12));
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(currentUser.isAdmin() ? new Color(146,64,14) : new Color(29,78,216));
        badge.setOpaque(false);
        JLabel dateL = new JLabel(new java.text.SimpleDateFormat("EEE, dd MMM yyyy").format(new java.util.Date()));
        dateL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateL.setForeground(new Color(107,114,128));
        rp.add(bell); rp.add(badge); rp.add(dateL);
        bar.add(pageTitleLabel, BorderLayout.WEST);
        bar.add(rp, BorderLayout.EAST);
        return bar;
    }

    private JButton makeSidebarBtn(String icon, String text) {
        JButton btn = new JButton(icon + "   " + text) {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeBtn) {
                    g2.setColor(SIDEBAR_SEL); g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(new Color(147,197,253)); g2.fillRect(0,0,3,getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOV); g2.fillRect(0,0,getWidth(),getHeight());
                } else {
                    g2.setColor(SIDEBAR_BG); g2.fillRect(0,0,getWidth(),getHeight());
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11,20,11,20));
        btn.setMaximumSize(new Dimension(220,44));
        return btn;
    }

    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(new Color(71,85,105));
        l.setBorder(new EmptyBorder(4,0,4,0));
        l.setMaximumSize(new Dimension(220,22));
        return l;
    }

    private void setActive(JButton btn) {
        if (activeBtn != null) { activeBtn.setForeground(TEXT_MUTED); activeBtn.repaint(); }
        activeBtn = btn;
        btn.setForeground(Color.WHITE); btn.repaint();
    }

    private void show(JPanel panel, String title) {
        pageTitleLabel.setText(title);
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate(); contentArea.repaint();
    }

    private void showDashboard() { show(new DashboardPanel(currentUser), "Dashboard"); }
    private void showProducts()  { show(new ProductPanel(currentUser), "Product Management"); }
    private void showPOS()       { show(new POSPanel(currentUser), "Point of Sale"); }
    private void showHistory()   { show(new HistoryPanel(), "Sales History"); }
    private void showCustomers() { show(new CustomerPanel(), "Customer Management"); }
    private void showUsers()     { show(new UserPanel(), "User Management"); }
    private void showSuppliers() { show(new SupplierPanel(), "Supplier Management"); }
    private void showReports()   { show(new ReportsPanel(), "Sales Reports"); }
    private void showSettings()  {
        show(new SettingsPanel(currentUser), "Settings");
        SwingUtilities.invokeLater(() -> {
            ShopSettings.load();
            sidebarShopName.setText(ShopSettings.getShopName());
            setTitle(ShopSettings.getShopName() + " — " + currentUser.getFullName());
        });
    }
}