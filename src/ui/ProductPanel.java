package ui;

import model.Product;
import model.User;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private static final Color BG       = new Color(248, 250, 252);
    private static final Color WHITE    = Color.WHITE;
    private static final Color PRIMARY  = new Color(59, 130, 246);
    private static final Color SUCCESS  = new Color(16, 185, 129);
    private static final Color DANGER   = new Color(239, 68, 68);
    private static final Color BORDER_C = new Color(226, 232, 240);
    private static final Color TEXT_D   = new Color(15, 23, 42);
    private static final Color TEXT_G   = new Color(100, 116, 139);

    private JTable table;
    private JTextField searchTF;
    private User currentUser;

    public ProductPanel(User user) {
        this.currentUser = user;
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top bar: search + buttons
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Search box
        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setBackground(WHITE);
        searchBox.setBorder(new LineBorder(BORDER_C, 1, true));
        searchBox.setPreferredSize(new Dimension(300, 38));

        JLabel searchIcon = new JLabel("  🔍 ");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchTF = new JTextField();
        searchTF.setBorder(null);
        searchTF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTF.setBackground(WHITE);
        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchTF, BorderLayout.CENTER);

        searchTF.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String kw = searchTF.getText().trim();
                if (kw.isEmpty()) loadTable();
                else table.setModel(Product.searchProducts(kw));
            }
        });

        topBar.add(searchBox, BorderLayout.WEST);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton refreshBtn = makeButton("↻ Refresh", new Color(100, 116, 139));
        JButton addBtn     = makeButton("+ Add Product", SUCCESS);
        JButton editBtn    = makeButton("✏ Edit", PRIMARY);
        JButton deleteBtn  = makeButton("🗑 Delete", DANGER);

        refreshBtn.addActionListener(e -> loadTable());
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        btnPanel.add(refreshBtn);
        if (user.isAdmin()) {
            btnPanel.add(addBtn);
            btnPanel.add(editBtn);
            btnPanel.add(deleteBtn);
        }
        topBar.add(btnPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        table = new JTable();
        styleTable(table);
        loadTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_C, 1, true));
        scroll.getViewport().setBackground(WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadTable() {
        table.setModel(Product.getAllProducts());
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Product", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(WHITE);

        JTextField nameTF     = new JTextField();
        JTextField priceTF    = new JTextField();
        JTextField costTF     = new JTextField();
        JTextField qtyTF      = new JTextField();
        JTextField limitTF    = new JTextField("5");
        JTextField barcodeTF  = new JTextField();

        // Category combo
        JComboBox<String> catCB = new JComboBox<>();
        List<String[]> cats = Product.getCategories();
        
        for (String[] c : cats) {
            String display = c[0] + "-" + c[1];
            catCB.addItem(display);
        }

        form.add(new JLabel("Product Name:")); form.add(nameTF);
        form.add(new JLabel("Category:"));     form.add(catCB);
        form.add(new JLabel("Selling Price:")); form.add(priceTF);
        form.add(new JLabel("Cost Price:"));   form.add(costTF);
        form.add(new JLabel("Quantity:"));     form.add(qtyTF);
        form.add(new JLabel("Low Stock Limit:")); form.add(limitTF);
        form.add(new JLabel("Barcode:"));      form.add(barcodeTF);

        JButton saveBtn = makeButton("Save Product", SUCCESS);
        saveBtn.addActionListener(e -> {
            try {
                String name = nameTF.getText().trim();
                String priceText = priceTF.getText().trim();
                String qtyText = qtyTF.getText().trim();
                
                if (name.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name, Price and Quantity are required!");
                    return;
                }
                
                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(qtyText);
                double cost = costTF.getText().trim().isEmpty() ? 0 : Double.parseDouble(costTF.getText().trim());
                int lowLimit = limitTF.getText().trim().isEmpty() ? 5 : Integer.parseInt(limitTF.getText().trim());
                String barcode = barcodeTF.getText().trim();
                
                // Get category ID
                int catId = 1;
                String selected = (String) catCB.getSelectedItem();
                if (selected != null && !selected.isEmpty()) {
                    String[] parts = selected.split("-");
                    if (parts.length > 0) {
                        try {
                            catId = Integer.parseInt(parts[0].trim());
                        } catch (NumberFormatException ex) {
                            catId = 1;
                        }
                    }
                }
                
                boolean ok = Product.addProduct(name, catId, price, cost, quantity, lowLimit, barcode);
                
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                    loadTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add product. Check console for errors.");
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for price, cost, quantity.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(WHITE);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { 
            JOptionPane.showMessageDialog(this, "Please select a product first."); 
            return; 
        }
        int productId = (int) table.getValueAt(row, 0);
        Product p = Product.getById(productId);
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Product", true);
        dialog.setSize(420, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(WHITE);

        JTextField nameTF  = new JTextField(p.getName());
        JTextField priceTF = new JTextField(String.valueOf(p.getPrice()));
        JTextField costTF  = new JTextField(String.valueOf(p.getCostPrice()));
        JTextField qtyTF   = new JTextField(String.valueOf(p.getQuantity()));
        JTextField limitTF = new JTextField(String.valueOf(p.getLowStockLimit()));

        JComboBox<String> catCB = new JComboBox<>();
        List<String[]> cats = Product.getCategories();
        int selectedIndex = 0;
        int counter = 0;
        
        for (String[] c : cats) {
            String display = c[0] + "-" + c[1];
            catCB.addItem(display);
            if (p.getCategory() != null && c[1].equals(p.getCategory())) {
                selectedIndex = counter;
            }
            counter++;
        }
        
        if (!cats.isEmpty()) {
            catCB.setSelectedIndex(selectedIndex);
        }

        form.add(new JLabel("Product Name:")); form.add(nameTF);
        form.add(new JLabel("Category:"));     form.add(catCB);
        form.add(new JLabel("Selling Price:")); form.add(priceTF);
        form.add(new JLabel("Cost Price:"));   form.add(costTF);
        form.add(new JLabel("Quantity:"));     form.add(qtyTF);
        form.add(new JLabel("Low Stock Limit:")); form.add(limitTF);

        JButton saveBtn = makeButton("Update Product", PRIMARY);
        saveBtn.addActionListener(e -> {
            try {
                String name = nameTF.getText().trim();
                double price = Double.parseDouble(priceTF.getText().trim());
                double cost = costTF.getText().trim().isEmpty() ? 0 : Double.parseDouble(costTF.getText().trim());
                int quantity = Integer.parseInt(qtyTF.getText().trim());
                int lowLimit = Integer.parseInt(limitTF.getText().trim());
                
                int catId = 1;
                String selected = (String) catCB.getSelectedItem();
                if (selected != null && !selected.isEmpty()) {
                    String[] parts = selected.split("-");
                    if (parts.length > 0) {
                        catId = Integer.parseInt(parts[0].trim());
                    }
                }
                
                boolean ok = Product.updateProduct(productId, name, catId, price, cost, quantity, lowLimit);
                
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Product updated!");
                    loadTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(WHITE);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { 
            JOptionPane.showMessageDialog(this, "Please select a product first."); 
            return; 
        }
        int productId = (int) table.getValueAt(row, 0);
        String name   = (String) table.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete product: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Product.deleteProduct(productId)) {
                JOptionPane.showMessageDialog(this, "Product deleted.");
                loadTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.");
            }
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT_D);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(TEXT_G);
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
    }
}