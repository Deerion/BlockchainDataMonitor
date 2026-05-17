package pl.skompilowani.shared.ui;

import pl.skompilowani.core.model.TransactionDTO;
import pl.skompilowani.shared.util.DateFormatter;
import pl.skompilowani.shared.util.FormatConstants;
import pl.skompilowani.shared.util.HashShortener;

import java.util.List;

public class TableFormatter {

    private static final GenericTablePrinter<TransactionDTO> PRINTER = new GenericTablePrinter<>(
            FormatConstants.TABLE_WIDTH,
            List.of(
                    new GenericTablePrinter.ColumnDefinition<>("Hash", 15, tx -> HashShortener.shorten(tx.hash()), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Od", 15, tx -> HashShortener.shorten(tx.from()), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Do", 15, tx -> HashShortener.shorten(tx.to() != null ? tx.to() : "Tworzenie Kontr."), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Wartość ETH", 12, TransactionDTO::valueEth, ".6f"),
                    new GenericTablePrinter.ColumnDefinition<>("Zużyty gaz", 10, TransactionDTO::gasUsed, "d"),
                    new GenericTablePrinter.ColumnDefinition<>("Opłata ETH", 12, TransactionDTO::oplataEth, ".6f"),
                    new GenericTablePrinter.ColumnDefinition<>("Data", 20, tx -> DateFormatter.format(tx.timestamp()), "s")
            )
    );

    public static void printTransactionsTable(List<TransactionDTO> transactions) {
        PRINTER.printTable(transactions);
    }
}