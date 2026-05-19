package pl.skompilowani.shared.ui;

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

        System.out.print("\r" + bar.toString());

        if (current == total) {
            System.out.println();
        }
    }
}