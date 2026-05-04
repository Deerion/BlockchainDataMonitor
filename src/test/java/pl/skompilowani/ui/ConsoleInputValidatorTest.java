package pl.skompilowani.ui;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleInputValidatorTest {

    @Test
    void shouldReturnValidIntAfterTypingLetters() {
        // Symulujemy wpisanie "abc" (błąd), potem "99" (poza zakresem), a na końcu "2" (poprawne)
        String input = "abc\n99\n2\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Scanner scanner = new Scanner(System.in);
        ConsoleInputValidator validator = new ConsoleInputValidator(scanner);

        // Testujemy wybór z menu 1-5
        int result = validator.getValidInt("Wybierz opcję: ", 1, 5);

        assertEquals(2, result);
    }
}