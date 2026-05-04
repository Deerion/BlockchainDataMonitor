package pl.skompilowani.util;

public class ProgressBar {

    public static void show(int current, int total, String message) {
        int barLength = 20;
        double percentage = (double) current / total;
        int filledLength = (int) (barLength * percentage);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append(TerminalColorizer.green("#"));
            } else {
                bar.append("-");
            }
        }
        bar.append("] ")
                .append(String.format("%3d%%", (int) (percentage * 100))) // Wyrównanie do 3 znaków np. '  5%'
                .append(" | ").append(message);

        // \r cofa kursor na początek linii, nadpisując stary pasek
        System.out.print("\r" + bar.toString());

        // Jeśli doszliśmy do końca, przeskocz do nowej linii, żeby nie nadpisać paska kolejnym tekstem
        if (current == total) {
            System.out.println();
        }
    }
}