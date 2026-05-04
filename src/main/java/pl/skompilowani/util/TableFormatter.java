package pl.skompilowani.util;

import pl.skompilowani.service.dto.TransactionDTO;
import java.util.List;

public class TableFormatter {

    public static void printTransactionsTable(List<TransactionDTO> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("Brak transakcji do wyświetlenia w tabeli.");
            return;
        }

        // Rysujemy nagłówek tabeli
        System.out.println("-".repeat(95));
        System.out.println(String.format("| %-15s | %-15s | %-15s | %-12s | %-10s | %-12s |",
                "Hash", "Od", "Do", "Wartość ETH", "Gas", "Data"));
        System.out.println("-".repeat(95));

        // Pętla rysująca poszczególne wiersze transakcji
        for (TransactionDTO tx : transactions) {
            String toAddress = tx.to() != null ? tx.to() : "Tworzenie Kontr.";

            // Formatujemy poszczególne pola używając Twoich wcześniejszych narzędzi
            String formattedRow = String.format("| %-15s | %-15s | %-15s | %-12.6f | %-10d | %-12s |",
                    HashShortener.shorten(tx.hash()),
                    HashShortener.shorten(tx.from()),
                    HashShortener.shorten(toAddress),
                    tx.valueEth(),
                    tx.gasUsed(),
                    // Formatujemy datę, a potem ucinamy do 12 znaków żeby nie rozwaliło tabeli (np. 2026-05-04)
                    DateFormatter.format(tx.timestamp()).substring(0, 10)
            );
            System.out.println(formattedRow);
        }
        System.out.println("-".repeat(95));
    }
}