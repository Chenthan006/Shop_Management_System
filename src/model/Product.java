package model;

import util.DBConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private double price;
    private double costPrice;
    private int quantity;
    private int lowStockLimit;
    private String barcode;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public String getCategory() { return categoryName; }
    public double getPrice() { return price; }
    public double getCostPrice() { return costPrice; }
    public int getQuantity() { return quantity; }
    public int getLowStockLimit() { return lowStockLimit; }
    public String getBarcode() { return barcode; }
    public boolean isLowStock() { return quantity <= lowStockLimit; }

    public static DefaultTableModel getAllProducts() {
        String[] cols = {"ID", "Name", "Category", "Price", "Stock", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        String sql = "SELECT p.id, p.name, c.name as category, p.price, p.quantity, p.low_stock_limit " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                int limit = rs.getInt("low_stock_limit");
                String status = qty <= limit ? "Low Stock" : "OK";
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    qty,
                    status
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static DefaultTableModel searchProducts(String keyword) {
        String[] cols = {"ID", "Name", "Category", "Price", "Stock", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        String sql = "SELECT p.id, p.name, c.name as category, p.price, p.quantity, p.low_stock_limit " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.name LIKE ? OR p.barcode LIKE ? ORDER BY p.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                int limit = rs.getInt("low_stock_limit");
                String status = qty <= limit ? "Low Stock" : "OK";
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    qty,
                    status
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static Product getById(int id) {
        String sql = "SELECT p.*, c.name as category FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.id = rs.getInt("id");
                p.name = rs.getString("name");
                p.categoryId = rs.getInt("category_id");
                p.categoryName = rs.getString("category");
                p.price = rs.getDouble("price");
                p.costPrice = rs.getDouble("cost_price");
                p.quantity = rs.getInt("quantity");
                p.lowStockLimit = rs.getInt("low_stock_limit");
                p.barcode = rs.getString("barcode");
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addProduct(String name, int categoryId, double price, double costPrice, 
                                     int quantity, int lowLimit, String barcode) {
        String sql = "INSERT INTO products (name, category_id, price, cost_price, quantity, low_stock_limit, barcode) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setDouble(3, price);
            ps.setDouble(4, costPrice);
            ps.setInt(5, quantity);
            ps.setInt(6, lowLimit);
            ps.setString(7, barcode);
            
            int result = ps.executeUpdate();
            System.out.println("Product added: " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProduct(int id, String name, int categoryId, double price, 
                                        double costPrice, int quantity, int lowLimit) {
        String sql = "UPDATE products SET name=?, category_id=?, price=?, cost_price=?, quantity=?, low_stock_limit=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setDouble(3, price);
            ps.setDouble(4, costPrice);
            ps.setInt(5, quantity);
            ps.setInt(6, lowLimit);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String[]> getCategories() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, name FROM categories ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("name")});
                System.out.println("Category: " + rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deductStock(int productId, int quantity) {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================
    // GET LOW STOCK PRODUCTS FOR DASHBOARD
    // ============================================
    public static DefaultTableModel getLowStockProducts() {
        String[] cols = {"ID", "Product Name", "Current Stock", "Min Required", "Need to Order"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        String sql = "SELECT id, name, quantity, low_stock_limit, " +
                     "(low_stock_limit - quantity) as need " +
                     "FROM products WHERE quantity <= low_stock_limit";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getInt("low_stock_limit"),
                    rs.getInt("need")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }
}