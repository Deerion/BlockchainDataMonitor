package pl.skompilowani.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import pl.skompilowani.service.SessionStatisticsService;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;

import java.awt.Color;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportGenerator {

    private static final Color HEADER_BG = new Color(26, 35, 126);
    private static final Color BLOCK_HEADER_BG = new Color(232, 234, 246);
    private static final Color ROW_ALT = new Color(245, 245, 245);
    private static final Color TEXT_MAIN = new Color(33, 33, 33);

    public static void generateReport(List<BlockDTO> blocks, BigDecimal avgGasPrice, SessionStatisticsService stats) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_analityczny_" + timestamp + ".pdf");

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // KLUCZ: Definicja czcionek z obsługą polskich znaków (CP1250)
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1250, BaseFont.EMBEDDED);
            BaseFont bfMono = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1250, BaseFont.EMBEDDED);

            Font titleFont = new Font(bfBold, 22, Font.NORMAL, HEADER_BG);
            Font subTitleFont = new Font(bf, 10, Font.NORMAL, Color.GRAY);
            Font labelFont = new Font(bfBold, 7, Font.NORMAL, Color.DARK_GRAY);
            Font valueFont = new Font(bfBold, 11, Font.NORMAL, HEADER_BG);
            Font blockFont = new Font(bfBold, 9, Font.NORMAL, HEADER_BG);
            Font headerTableFont = new Font(bfBold, 8, Font.NORMAL, Color.WHITE);
            Font dataFont = new Font(bf, 8, Font.NORMAL, TEXT_MAIN);
            Font hexFont = new Font(bfMono, 7, Font.NORMAL, TEXT_MAIN);

            // 1. Nagłówek dokumentu
            Paragraph title = new Paragraph("ZAAWANSOWANY RAPORT ANALITYCZNY BLOCKCHAIN", titleFont);
            title.setSpacingAfter(5);
            document.add(title);

            document.add(new Paragraph("Sieć: Ethereum Sepolia | Data generowania: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), subTitleFont));
            document.add(new Paragraph(" "));

            // 2. Dashboard statystyk
            PdfPTable summary = new PdfPTable(4);
            summary.setWidthPercentage(100);
            addStatCell(summary, "Przeanalizowane bloki", String.valueOf(blocks.size()), labelFont, valueFont);
            addStatCell(summary, "Średnia cena Gas (Wei)", avgGasPrice.setScale(2, RoundingMode.HALF_UP).toPlainString(), labelFont, valueFont);
            addStatCell(summary, "Transakcji w raporcie", String.valueOf(blocks.stream().mapToInt(BlockDTO::transactionCount).sum()), labelFont, valueFont);
            addStatCell(summary, "Czas trwania sesji", stats.getSessionDuration(), labelFont, valueFont);
            document.add(summary);
            document.add(new Paragraph(" "));

            // 3. Tabele danych
            for (BlockDTO block : blocks) {
                PdfPTable table = new PdfPTable(new float[]{24, 18, 18, 10, 8, 10, 12});
                table.setWidthPercentage(100);
                table.setSpacingBefore(8);

                String blockInfoText = String.format("BLOK: %d | Hash: %s | Ilość Tx: %d",
                        block.number(), block.hash(), block.transactionCount());

                PdfPCell blockCell = new PdfPCell(new Phrase(blockInfoText, blockFont));
                blockCell.setColspan(7);
                blockCell.setPadding(7);
                blockCell.setBackgroundColor(BLOCK_HEADER_BG);
                blockCell.setBorderColor(HEADER_BG);
                blockCell.setBorderWidthLeft(4f);
                table.addCell(blockCell);

                if (block.transactions() != null && !block.transactions().isEmpty()) {
                    // Poprawione nagłówki z polskimi znakami
                    String[] headers = {"Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data i Czas"};
                    for (String h : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(h, headerTableFont));
                        cell.setBackgroundColor(HEADER_BG);
                        cell.setPadding(4);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                    }

                    table.setHeaderRows(2);
                    table.setKeepTogether(true);

                    boolean alt = false;
                    for (TransactionDTO tx : block.transactions()) {
                        addTxCell(table, tx.hash(), hexFont, alt);
                        addTxCell(table, tx.from(), hexFont, alt);
                        addTxCell(table, tx.to() != null ? tx.to() : "Tworzenie Kontaktu", hexFont, alt);

                        String formattedValue = tx.valueEth().setScale(6, RoundingMode.HALF_UP).toPlainString();
                        String formattedFee = tx.oplataEth().setScale(6, RoundingMode.HALF_UP).toPlainString();

                        addTxCell(table, formattedValue, dataFont, alt);
                        addTxCell(table, String.valueOf(tx.gasUsed()), dataFont, alt);
                        addTxCell(table, formattedFee, dataFont, alt);
                        addTxCell(table, DateFormatter.format(tx.timestamp()), dataFont, alt);
                        alt = !alt;
                    }
                }
                document.add(table);
            }

            document.close();
            System.out.println(TerminalColorizer.green("\nSukces! Profesjonalny raport PDF zapisany: " + filePath.toAbsolutePath()));

        } catch (Exception e) {
            System.err.println("Błąd PDF: " + e.getMessage());
        }
    }

    private static void addStatCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBackgroundColor(new Color(240, 242, 245));
        cell.setBorder(0);
        cell.addElement(new Phrase(label.toUpperCase(), labelFont));
        cell.addElement(new Phrase(value, valueFont));
        table.addCell(cell);
    }

    private static void addTxCell(PdfPTable table, String content, Font font, boolean alt) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(4);
        cell.setBorder(0);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColorBottom(Color.LIGHT_GRAY);
        if (alt) cell.setBackgroundColor(ROW_ALT);
        table.addCell(cell);
    }
}