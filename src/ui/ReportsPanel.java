package ui;

import util.DBConnection;
import model.ShopSettings;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportsPanel extends JPanel {

    private static final Color BG      = new Color(248, 250, 252);
    private static final Color WHITE   = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color BORDER  = new Color(226, 232, 240);
    private static final Color TEXT_D  = new Color(15, 23, 42);
    private static final Color TEXT_G  = new Color(100, 116, 139);

    private JTable table;
    private JTextField fromTF, toTF;
    private JLabel totalRevenueL, totalSalesL, avgSaleL;

    public ReportsPanel() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top filter
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        fromTF = makeField("2026-01-01");
        toTF   = makeField("2026-12-31");

        JButton genBtn  = makeBtn("📊 Generate Report", PRIMARY);
        JButton pdfBtn  = makeBtn("📄 Export PDF", SUCCESS);

        JLabel fromL = new JLabel("From:");
        fromL.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        fromL.setForeground(TEXT_D);
        
        JLabel toL = new JLabel("To:");
        toL.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        toL.setForeground(TEXT_D);

        filterBar.add(fromL);  filterBar.add(fromTF);
        filterBar.add(toL);    filterBar.add(toTF);
        filterBar.add(genBtn); filterBar.add(pdfBtn);

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);

        totalRevenueL = new JLabel("Rs. 0.00");
        totalSalesL   = new JLabel("0");
        avgSaleL      = new JLabel("Rs. 0.00");

        summaryRow.add(makeSummaryCard("💰", "Total Revenue", totalRevenueL, new Color(99, 102, 241), new Color(79, 70, 229)));
        summaryRow.add(makeSummaryCard("🧾", "Total Transactions", totalSalesL, new Color(16, 185, 129), new Color(5, 150, 105)));
        summaryRow.add(makeSummaryCard("📊", "Average Sale", avgSaleL, new Color(236, 72, 153), new Color(219, 39, 119)));

        // Table
        table = new JTable();
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(1, 1, 1, 1)
        ));
        scroll.getViewport().setBackground(WHITE);

        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setOpaque(false);
        topSection.add(filterBar, BorderLayout.NORTH);
        topSection.add(summaryRow, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        genBtn.addActionListener(e -> generateReport());
        pdfBtn.addActionListener(e -> exportPDF());

        generateReport(); // load default
    }

    private void generateReport() {
        String from = fromTF.getText().trim();
        String to   = toTF.getText().trim();

        String[] cols = {"Date", "Invoice", "Customer", "Cashier", "Subtotal", "Discount", "Total", "Payment"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        double totalRev = 0; int totalTrans = 0;
        String sql = "SELECT DATE(s.sale_date), s.invoice_number, c.name, u.full_name, " +
                     "s.subtotal, s.discount, s.total, s.payment_method " +
                     "FROM sales s " +
                     "JOIN customers c ON s.customer_id = c.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                     "ORDER BY s.sale_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double total = rs.getDouble(7);
                totalRev += total; totalTrans++;
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    String.format("%.2f", rs.getDouble(5)),
                    String.format("%.2f", rs.getDouble(6)),
                    String.format("%.2f", total),
                    rs.getString(8)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }

        table.setModel(model);
        totalRevenueL.setText(String.format("Rs. %,.2f", totalRev));
        totalSalesL.setText(String.valueOf(totalTrans));
        avgSaleL.setText(totalTrans > 0 ? String.format("Rs. %.2f", totalRev / totalTrans) : "Rs. 0.00");
    }

    private void exportPDF() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export! Generate report first.");
            return;
        }
        try {
            String fileName = System.getProperty("user.home") + "/Desktop/SalesReport_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";

            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            BaseColor blue = new BaseColor(30, 58, 138);
            com.itextpdf.text.Font titleF  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, blue);
            com.itextpdf.text.Font subF    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9,  com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);
            com.itextpdf.text.Font headF   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9,  com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
            com.itextpdf.text.Font cellF   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,  com.itextpdf.text.Font.NORMAL, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font boldF   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, blue);

            // Header
            Paragraph title = new Paragraph(ShopSettings.getShopName(), titleF);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            Paragraph sub = new Paragraph("Sales Report  |  " +
                fromTF.getText() + " to " + toTF.getText() + "  |  Generated: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subF);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(10);
            doc.add(sub);

            // Summary
            PdfPTable sumTable = new PdfPTable(3);
            sumTable.setWidthPercentage(80);
            sumTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            sumTable.setSpacingAfter(12);

            addSumCell(sumTable, "Total Revenue: " + totalRevenueL.getText(), boldF);
            addSumCell(sumTable, "Transactions: " + totalSalesL.getText(), boldF);
            addSumCell(sumTable, "Average Sale: " + avgSaleL.getText(), boldF);
            doc.add(sumTable);

            // Data table
            PdfPTable dataTable = new PdfPTable(8);
            dataTable.setWidthPercentage(100);
            dataTable.setWidths(new float[]{1.2f,1.8f,1.5f,1.5f,1.2f,1f,1.2f,1f});

            String[] headers = {"Date","Invoice","Customer","Cashier","Subtotal","Discount","Total","Payment"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, headF));
                c.setBackgroundColor(blue); c.setPadding(5);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                dataTable.addCell(c);
            }

            boolean alt = false;
            for (int i = 0; i < table.getRowCount(); i++) {
                BaseColor bg = alt ? new BaseColor(248,250,252) : BaseColor.WHITE;
                for (int j = 0; j < 8; j++) {
                    PdfPCell c = new PdfPCell(new Phrase(
                        table.getValueAt(i,j) != null ? table.getValueAt(i,j).toString() : "", cellF));
                    c.setBackgroundColor(bg); c.setPadding(4);
                    dataTable.addCell(c);
                }
                alt = !alt;
            }
            doc.add(dataTable);
            doc.close();

            Desktop.getDesktop().open(new File(fileName));
            JOptionPane.showMessageDialog(this, "✅ Report exported to Desktop!\n" + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to export: " + e.getMessage());
        }
    }

    private void addSumCell(PdfPTable t, String text, com.itextpdf.text.Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new BaseColor(239,246,255));
        c.setPadding(8); c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorderColor(new BaseColor(191,219,254));
        t.addCell(c);
    }

    private JPanel makeSummaryCard(String emoji, String title, JLabel valueL, Color startColor, Color endColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, h, endColor);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 16, 16));
                
                // Top-shine white outline
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Left section (text)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 4, 0));
        leftPanel.setOpaque(false);

        JLabel tl = new JLabel(title);
        tl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        tl.setForeground(new Color(255, 255, 255, 200));

        valueL.setFont(new Font("Segoe UI Variable", Font.BOLD, 22));
        valueL.setForeground(Color.WHITE);

        leftPanel.add(tl);
        leftPanel.add(valueL);

        // Right section (circular emoji badge)
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                int size = Math.min(getWidth(), getHeight());
                g2.fillOval((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
                g2.dispose();
            }
        };
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(42, 42));

        JLabel el = new JLabel(emoji, SwingConstants.CENTER);
        el.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        rightPanel.add(el);

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JTextField makeField(String text) {
        JTextField tf = new JTextField(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isFocusOwner()) {
                    g2.setColor(PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(BORDER);
                }
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(110, 34));
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(2, 10, 2, 10));
        return tf;
    }

    private JButton makeBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = color;
                if (hovered) {
                    c = new Color(Math.max(0, Math.min(255, color.getRed() + 20)),
                                  Math.max(0, Math.min(255, color.getGreen() + 20)),
                                  Math.max(0, Math.min(255, color.getBlue() + 20)));
                }
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
                
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                g2.drawString(getText(), (w - textW) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 36));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(239, 246, 255));
        t.setSelectionForeground(TEXT_D);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 0));

        t.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (isSelected) {
                    setBackground(new Color(239, 246, 255));
                    setForeground(TEXT_D);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                    setForeground(TEXT_D);
                }
                return this;
            }
        });

        t.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(TEXT_G);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        
        t.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(248, 250, 252));
                setForeground(TEXT_G);
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 12, 0, 12)
                ));
                return this;
            }
        });
    }
}