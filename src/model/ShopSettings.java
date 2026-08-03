package model;

import util.DBConnection;
import java.sql.*;

public class ShopSettings {

    private static String shopName   = "ShopManager Pro";
    private static String address    = "";
    private static String phone      = "";
    private static String email      = "";
    private static double taxPercent = 0.0;

    // Load settings from DB
    public static void load() {
        String sql = "SELECT * FROM shop_settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                shopName   = rs.getString("shop_name");
                address    = rs.getString("address");
                phone      = rs.getString("phone");
                email      = rs.getString("email");
                taxPercent = rs.getDouble("tax_percent");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Save settings to DB
    public static boolean save(String name, String addr, String ph, String em, double tax) {
        String sql = "UPDATE shop_settings SET shop_name=?, address=?, phone=?, email=?, tax_percent=? WHERE id=1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, addr);
            ps.setString(3, ph);
            ps.setString(4, em);
            ps.setDouble(5, tax);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) { shopName = name; address = addr; phone = ph; email = em; taxPercent = tax; }
            return ok;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Getters
    public static String getShopName()   { return shopName; }
    public static String getAddress()    { return address; }
    public static String getPhone()      { return phone; }
    public static String getEmail()      { return email; }
    public static double getTaxPercent() { return taxPercent; }
}