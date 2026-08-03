package ui;

import model.Sale;
import model.Product;
import model.ShopSettings;
import model.User;
import util.DBConnection;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;

public class DashboardPanel extends JPanel {

    private static final Color BG       = new Color(243, 244, 246);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BLUE     = new Color(59, 130, 246);
    private static final Color GREEN    = new Color(16, 185, 129);
    private static final Color ORANGE   = new Color(245, 158, 11);
    private static final Color RED      = new Color(239, 68, 68);
    private static final Color PURPLE   = new Color(139, 92, 246);
    private static final Color TEXT_D   = new Color(15, 23, 42);
    private static final Color TEXT_G   = new Color(100, 116, 139);
    private static final Color BORDER_C = new Color(226, 232, 240);

    public DashboardPanel(User user) {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel mainScroll = new JPanel();
        mainScroll.setOpaque(false);
        mainScroll.setLayout(new BoxLayout(mainScroll, BoxLayout.Y_AXIS));

        // Welcome
        JPanel welcomeRow = new JPanel(new BorderLayout());
        welcomeRow.setOpaque(false);
        welcomeRow.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel welcomeL = new JLabel("Good day, " + user.getFullName() + " 👋  —  " + ShopSettings.getShopName());
        welcomeL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeL.setForeground(TEXT_G);
        welcomeRow.add(welcomeL, BorderLayout.WEST);
        mainScroll.add(welcomeRow);

        // Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        double rev   = Sale.getTodayRevenue();
        int trans    = Sale.getTodayTransactions();
        int products = Sale.getTotalProducts();
        int lowStock = Sale.getLowStockCount();

        cardsRow.add(makeCard("Today's Revenue", String.format("Rs. %,.2f", rev), "💰", BLUE, "Sales today", new Color(219,234,254)));
        cardsRow.add(makeCard("Transactions", String.valueOf(trans), "🧾", GREEN, "Today", new Color(209,250,229)));
        cardsRow.add(makeCard("Total Products", String.valueOf(products), "📦", ORANGE, "In catalog", new Color(254,243,199)));
        cardsRow.add(makeCard("Low Stock Alerts", String.valueOf(lowStock), "⚠️",
            lowStock > 0 ? RED : GREEN,
            lowStock > 0 ? "Needs restock!" : "All good",
            lowStock > 0 ? new Color(254,226,226) : new Color(209,250,229)));

        mainScroll.add(cardsRow);
        mainScroll.add(Box.createVerticalStrut(16));

        // Chart + Low stock
        JPanel midRow = new JPanel(new GridLayout(1, 2, 14, 0));
        midRow.setOpaque(false);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // Chart card
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 16, 16, 16)));
        JLabel chartTitle = new JLabel("📊  Last 7 Days Revenue");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartTitle.setForeground(TEXT_D);
        chartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(new SalesBarChart(), BorderLayout.CENTER);

        // Low stock card
        JPanel lowCard = new JPanel(new BorderLayout());
        lowCard.setBackground(WHITE);
        lowCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 16, 16, 16)));
        JLabel lowTitle = new JLabel("⚠  Low Stock Products");
        lowTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lowTitle.setForeground(RED);
        lowTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        lowCard.add(lowTitle, BorderLayout.NORTH);

        JTable lowTable = new JTable(Product.getLowStockProducts());
        lowTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lowTable.setRowHeight(30); lowTable.setShowVerticalLines(false);
        lowTable.setGridColor(new Color(241,245,249));
        lowTable.setFillsViewportHeight(true);
        lowTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        lowTable.getTableHeader().setBackground(BG);
        lowTable.getTableHeader().setForeground(TEXT_G);
        JScrollPane ls = new JScrollPane(lowTable);
        ls.setBorder(null); ls.getViewport().setBackground(WHITE);
        lowCard.add(ls, BorderLayout.CENTER);

        midRow.add(chartCard);
        midRow.add(lowCard);
        mainScroll.add(midRow);
        mainScroll.add(Box.createVerticalStrut(16));

        // Quick actions
        JPanel qaCard = new JPanel(new BorderLayout());
        qaCard.setBackground(WHITE);
        qaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        qaCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true), new EmptyBorder(14, 16, 14, 16)));
        JLabel qaTitle = new JLabel("⚡  Quick Actions");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qaTitle.setForeground(TEXT_D);
        JPanel qaButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qaButtons.setOpaque(false);
        qaButtons.add(makeQABtn("🛒  New Sale", BLUE));
        qaButtons.add(makeQABtn("📦  Add Product", GREEN));
        qaButtons.add(makeQABtn("👥  Add Customer", PURPLE));
        qaButtons.add(makeQABtn("📊  View Reports", ORANGE));
        qaCard.add(qaTitle, BorderLayout.WEST);
        qaCard.add(qaButtons, BorderLayout.CENTER);
        mainScroll.add(qaCard);

        JScrollPane scrollPane = new JScrollPane(mainScroll);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel makeCard(String title, String value, String icon,
                             Color accent, String sub, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 18, 16, 18)));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel tl = new JLabel(title);
        tl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tl.setForeground(TEXT_G);
        JLabel il = new JLabel(icon);
        il.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        top.add(tl, BorderLayout.WEST); top.add(il, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        JLabel vl = new JLabel(value);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        vl.setForeground(accent);
        vl.setBorder(new EmptyBorder(8,0,4,0));
        card.add(vl, BorderLayout.CENTER);
        JLabel sl = new JLabel(sub);
        sl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sl.setForeground(TEXT_G);
        card.add(sl, BorderLayout.SOUTH);
        return card;
    }

    private JButton makeQABtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    // Bar Chart
    class SalesBarChart extends JPanel {
        private LinkedHashMap<String, Double> data = new LinkedHashMap<>();

        SalesBarChart() {
            setBackground(WHITE);
            setPreferredSize(new Dimension(300, 180));
            String sql = "SELECT DATE_FORMAT(sale_date,'%a') AS day, IFNULL(SUM(total),0) AS rev " +
                         "FROM sales WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                         "GROUP BY DATE(sale_date), day ORDER BY DATE(sale_date)";
            try (Connection conn = DBConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) data.put(rs.getString("day"), rs.getDouble("rev"));
            } catch (SQLException e) { e.printStackTrace(); }
            String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            for (String d : days) if (!data.containsKey(d)) data.put(d, 0.0);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int pL=50, pR=10, pT=10, pB=28;
            int cW = w-pL-pR, cH = h-pT-pB;
            double maxV = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            if (maxV == 0) maxV = 1;

            g2.setColor(new Color(241,245,249));
            for (int i=0; i<=4; i++) {
                int y = pT+(int)(cH*(1-i/4.0));
                g2.drawLine(pL, y, pL+cW, y);
                g2.setColor(TEXT_G);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(maxV*i/4>=1000 ? String.format("%.0fk",maxV*i/400) : String.format("%.0f",maxV*i/4), 2, y+4);
                g2.setColor(new Color(241,245,249));
            }

            int bc = data.size(), bw = (int)(cW/(bc*1.6));
            int gap = (cW-bw*bc)/(bc+1);
            int i=0;
            for (Map.Entry<String,Double> e : data.entrySet()) {
                double v = e.getValue();
                int bh = (int)(cH*(v/maxV));
                int x = pL+gap+i*(bw+gap), y = pT+cH-bh;
                GradientPaint gp = new GradientPaint(x,y,BLUE,x,y+bh,new Color(147,197,253));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(x,y,bw,bh,4,4));
                g2.setColor(TEXT_G);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(e.getKey(), x+bw/2-8, pT+cH+14);
                if (v>0) {
                    g2.setColor(BLUE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                    String vs = v>=1000 ? String.format("%.0fk",v/1000) : String.format("%.0f",v);
                    g2.drawString(vs, x+2, y-2);
                }
                i++;
            }
        }
    }
}