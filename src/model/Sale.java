package model;

import util.DBConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Sale {

    // Configure your MySQL password in DBConnection.java
    private static final String DB_URL = 
        "jdbc:mysql://localhost:3306/shop_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // Set your MySQL password here

    public static String generateInvoiceNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql  = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("INV-%s-%03d", date, count);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "INV-" + date + "-001";
    }

    public static boolean saveSale(int customerId, int userId, List<int[]> cartItems,
                                    double[] prices, double subtotal, double discount,
                                    double total, String paymentMethod) {
        String invoiceNo = generateInvoiceNumber();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            String saleSql = "INSERT INTO sales (invoice_number, customer_id, user_id, subtotal, discount, total, payment_method) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, invoiceNo);
            ps.setInt(2, customerId);
            ps.setInt(3, userId);
            ps.setDouble(4, subtotal);
            ps.setDouble(5, discount);
            ps.setDouble(6, total);
            ps.setString(7, paymentMethod);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (!keys.next()) { conn.rollback(); return false; }
            int saleId = keys.getInt(1);

            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES (?,?,?,?,?)";
            PreparedStatement ips = conn.prepareStatement(itemSql);
            for (int i = 0; i < cartItems.size(); i++) {
                int productId = cartItems.get(i)[0];
                int qty       = cartItems.get(i)[1];
                double unitP  = prices[i];
                ips.setInt(1, saleId);
                ips.setInt(2, productId);
                ips.setInt(3, qty);
                ips.setDouble(4, unitP);
                ips.setDouble(5, unitP * qty);
                ips.addBatch();

                PreparedStatement dps = conn.prepareStatement(
                    "UPDATE products SET quantity = quantity - ? WHERE id = ?");
                dps.setInt(1, qty);
                dps.setInt(2, productId);
                dps.executeUpdate();
            }
            ips.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double getTodayRevenue() {
        String sql = "SELECT IFNULL(SUM(total),0) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getTodayTransactions() {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= low_stock_limit";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static DefaultTableModel getSalesHistory() {
        String[] cols = {"Invoice", "Customer", "Cashier", "Total (Rs.)", "Payment", "Date & Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        String sql = "SELECT s.invoice_number, c.name, u.full_name, s.total, s.payment_method, s.sale_date " +
                     "FROM sales s " +
                     "JOIN customers c ON s.customer_id = c.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "ORDER BY s.sale_date DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    String.format("%.2f", rs.getDouble(4)),
                    rs.getString(5), rs.getTimestamp(6).toString()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return model;
    }
}