package pl.skompilowani.ui;

import java.util.Scanner;

public class ConsoleInputValidator {
    private final Scanner scanner;

    public ConsoleInputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    public int getValidInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Błąd: Proszę podać liczbę z zakresu od %d do %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Błąd: To nie jest poprawna cyfra! Spróbuj ponownie.");
            }
        }
    }
}