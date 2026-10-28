package cinema.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import cinema.model.Booking;

import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;

/**
 * Generates a styled PDF ticket using Apache PDFBox 3.x.
 */
public class PdfTicketGenerator {

    private static final PDColor DARK_BG   = toColor(new Color(18, 18, 40));
    private static final PDColor ACCENT    = toColor(new Color(99, 102, 241));
    private static final PDColor WHITE     = toColor(Color.WHITE);
    private static final PDColor LIGHT_GRAY = toColor(new Color(200, 200, 220));

    public static File generate(Booking b, File outputFile) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(500, 720));
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float W = page.getMediaBox().getWidth();
                float H = page.getMediaBox().getHeight();

                // ── Background ─────────────────────────────────────────────
                cs.setNonStrokingColor(DARK_BG);
                cs.addRect(0, 0, W, H);
                cs.fill();

                // ── Accent header bar ───────────────────────────────────────
                cs.setNonStrokingColor(ACCENT);
                cs.addRect(0, H - 100, W, 100);
                cs.fill();

                // ── CineBook title ──────────────────────────────────────────
                cs.setNonStrokingColor(WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 30);
                cs.newLineAtOffset(30, H - 55);
                cs.showText("CineBook");
                cs.endText();

                cs.setNonStrokingColor(LIGHT_GRAY);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(30, H - 75);
                cs.showText("Cinema Ticket Booking System");
                cs.endText();

                // Booking ID top right
                cs.setNonStrokingColor(WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.newLineAtOffset(W - 155, H - 55);
                cs.showText("#" + b.getBookingId());
                cs.endText();

                // ── Separator ───────────────────────────────────────────────
                cs.setStrokingColor(ACCENT);
                cs.setLineWidth(1.5f);
                cs.moveTo(30, H - 110);
                cs.lineTo(W - 30, H - 110);
                cs.stroke();

                // ── Movie title ─────────────────────────────────────────────
                cs.setNonStrokingColor(WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                cs.newLineAtOffset(30, H - 145);
                cs.showText(safe(b.getMovieTitle()));
                cs.endText();

                // ── Detail rows ─────────────────────────────────────────────
                float y = H - 190;
                float lineH = 28;

                row(cs, "Customer",       safe(b.getCustomerName()),    30, y);       y -= lineH;
                row(cs, "Show Date",      safe(b.getShowDate()),         30, y);       y -= lineH;
                row(cs, "Show Time",      safe(b.getShowTime()),         30, y);       y -= lineH;
                row(cs, "Hall",           safe(b.getHallName()),         30, y);       y -= lineH;
                row(cs, "Seats",          safe(b.getSeatList()),         30, y);       y -= lineH;
                row(cs, "Tickets",        String.valueOf(b.getTotalSeats()), 30, y);   y -= lineH;
                row(cs, "Food Add-ons",   safe(b.getFoodSummary()),      30, y);       y -= lineH;

                // ── Divider ──────────────────────────────────────────────────
                y -= 8;
                cs.setStrokingColor(ACCENT);
                cs.setLineWidth(1f);
                cs.moveTo(30, y);
                cs.lineTo(W - 30, y);
                cs.stroke();
                y -= 20;

                row(cs, "Payment",  safe(b.getPaymentMethod()), 30, y); y -= lineH;
                row(cs, "Txn ID",   safe(b.getTransactionId()), 30, y); y -= lineH;

                // ── Total ────────────────────────────────────────────────────
                y -= 6;
                cs.setNonStrokingColor(ACCENT);
                cs.addRect(28, y - 10, W - 56, 40);
                cs.fill();

                cs.setNonStrokingColor(WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(35, y + 10);
                cs.showText("Total Paid:  Rs. " + b.getTotalAmount());
                cs.endText();

                // ── Barcode placeholder ──────────────────────────────────────
                y -= 80;
                cs.setNonStrokingColor(WHITE);
                for (int i = 0; i < 40; i++) {
                    float bx = 30 + i * 10.8f;
                    float bw = (i % 3 == 0) ? 6f : 3f;
                    cs.addRect(bx, y, bw, 50);
                    cs.fill();
                }
                cs.setNonStrokingColor(LIGHT_GRAY);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                cs.newLineAtOffset(30, y - 14);
                cs.showText("CINEBOOK-BKG-" + b.getBookingId() + "-" + b.getShowDate().replace("-", ""));
                cs.endText();

                // ── Footer ────────────────────────────────────────────────────
                cs.setNonStrokingColor(LIGHT_GRAY);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
                cs.newLineAtOffset(30, 22);
                cs.showText("Thank you for choosing CineBook! Please arrive 15 minutes before showtime.");
                cs.endText();
            }

            doc.save(outputFile);
        }
        return outputFile;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static void row(PDPageContentStream cs, String label, String value,
                             float x, float y) throws Exception {
        cs.setNonStrokingColor(LIGHT_GRAY);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        cs.newLineAtOffset(x, y);
        cs.showText(label + ":");
        cs.endText();

        cs.setNonStrokingColor(WHITE);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
        cs.newLineAtOffset(x + 130, y);
        cs.showText(truncate(value, 40));
        cs.endText();
    }

    private static String safe(String s) { return (s == null || s.isBlank()) ? "—" : s; }
    private static String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) + "…" : s;
    }

    private static PDColor toColor(Color c) {
        return new PDColor(
            new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f},
            PDDeviceRGB.INSTANCE);
    }
}
