package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoicePrinter {

    private static final BaseColor PRIMARY   = new BaseColor(30, 58, 138);
    private static final BaseColor LIGHT_BG  = new BaseColor(248, 250, 252);
    private static final BaseColor BORDER    = new BaseColor(226, 232, 240);

    /**
     * Generate and open a PDF invoice
     * cartRows: List of String[]{"productName", "qty", "unitPrice", "total"}
     */
    public static void printInvoice(String invoiceNumber, String cashierName,
                                     String paymentMethod, List<String[]> cartRows,
                                     double subtotal, double discount, double total) {
        try {
            String fileName = System.getProperty("user.home") +
                              "/Desktop/Invoice_" + invoiceNumber + ".pdf";

            Document doc = new Document(PageSize.A5);
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            // Fonts
            Font titleFont    = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   PRIMARY);
            Font subFont      = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.GRAY);
            Font headerFont   = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,   BaseColor.WHITE);
            Font normalFont   = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.DARK_GRAY);
            Font boldFont     = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,   BaseColor.DARK_GRAY);
            Font totalFont    = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   PRIMARY);

            // Header
            Paragraph title = new Paragraph("ShopManager Pro", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("Shop Management System — Official Invoice", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(10);
           doc.add(sub);

            // Divider
          
            doc.add(new Paragraph(" "));

            // Invoice details
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(10);
            infoTable.setWidths(new float[]{1, 1});

            addInfoCell(infoTable, "Invoice No:", invoiceNumber, boldFont, normalFont);
            addInfoCell(infoTable, "Date:", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), boldFont, normalFont);
            addInfoCell(infoTable, "Cashier:", cashierName, boldFont, normalFont);
            addInfoCell(infoTable, "Payment:", paymentMethod, boldFont, normalFont);
            doc.add(infoTable);

            // Items table
            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{3, 1, 1.5f, 1.5f});
            itemTable.setSpacingAfter(10);

            // Table header
            String[] headers = {"Product", "Qty", "Price (Rs.)", "Total (Rs.)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(PRIMARY);
                cell.setPadding(6);
                cell.setBorderColor(PRIMARY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemTable.addCell(cell);
            }

            // Table rows
            boolean alternate = false;
            for (String[] row : cartRows) {
                BaseColor bg = alternate ? LIGHT_BG : BaseColor.WHITE;
                addItemCell(itemTable, row[0], normalFont, bg, Element.ALIGN_LEFT);
                addItemCell(itemTable, row[1], normalFont, bg, Element.ALIGN_CENTER);
                addItemCell(itemTable, row[2], normalFont, bg, Element.ALIGN_RIGHT);
                addItemCell(itemTable, row[3], normalFont, bg, Element.ALIGN_RIGHT);
                alternate = !alternate;
            }
            doc.add(itemTable);

            // Totals
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(60);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setWidths(new float[]{1.5f, 1});

            addTotalRow(totalTable, "Subtotal:", String.format("Rs. %.2f", subtotal), normalFont, boldFont);
            addTotalRow(totalTable, "Discount:", String.format("Rs. %.2f", discount), normalFont, boldFont);

            // Total row highlighted
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", totalFont));
            totalLabelCell.setBackgroundColor(LIGHT_BG);
            totalLabelCell.setPadding(8);
            totalLabelCell.setBorderColor(BORDER);
            totalTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("Rs. %.2f", total), totalFont));
            totalValueCell.setBackgroundColor(LIGHT_BG);
            totalValueCell.setPadding(8);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setBorderColor(BORDER);
            totalTable.addCell(totalValueCell);

            doc.add(totalTable);

            // Footer
            Paragraph footer = new Paragraph("\nThank you for your purchase!", subFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            doc.add(footer);

            doc.close();

            // Open PDF automatically
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                "Could not generate invoice: " + e.getMessage());
        }
    }

    private static void addInfoCell(PdfPTable table, String label, String value,
                                     Font labelFont, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(3);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(3);
        table.addCell(vc);
    }

    private static void addItemCell(PdfPTable table, String text, Font font,
                                     BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private static void addTotalRow(PdfPTable table, String label, String value,
                                     Font labelFont, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setPadding(5);
        lc.setBorderColor(BORDER);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setPadding(5);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorderColor(BORDER);
        table.addCell(vc);
    }
}