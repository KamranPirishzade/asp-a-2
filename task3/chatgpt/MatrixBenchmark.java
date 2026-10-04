import java.util.Arrays;

public final class MatrixBenchmark {

    private static int positiveInteger(
        String value,
        String name
    ) {
        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new IllegalArgumentException(
                    name + " must be positive."
                );
            }

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                name + " must be an integer."
            );
        }
    }

    private static double median(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);

        int middle = copy.length / 2;

        if (copy.length % 2 == 1) {
            return copy[middle];
        }

        return (
            copy[middle - 1] + copy[middle]
        ) / 2.0;
    }

    public static void main(String[] args) {

        if (args.length != 5) {
            System.err.println(
                "Usage:"
                    + " java MatrixBenchmark"
                    + " <rowsA> <shared> <colsB>"
                    + " <warmups> <runs>"
            );

            System.exit(1);
        }

        try {
            int rowsA =
                positiveInteger(args[0], "rowsA");

            int shared =
                positiveInteger(args[1], "shared");

            int colsB =
                positiveInteger(args[2], "colsB");

            int warmups =
                positiveInteger(args[3], "warmups");

            int runs =
                positiveInteger(args[4], "runs");

            Matrix a =
                Matrix.generate(rowsA, shared);

            Matrix b =
                Matrix.generate(shared, colsB);

            System.out.printf(
                "A: %d x %d%n",
                rowsA,
                shared
            );

            System.out.printf(
                "B: %d x %d%n",
                shared,
                colsB
            );

            System.out.printf(
                "Warm-ups: %d%n",
                warmups
            );

            System.out.printf(
                "Measured runs: %d%n%n",
                runs
            );

            /*
             * Warm-up phase.
             *
             * Results are deliberately used so the multiplication
             * is not completely irrelevant to the program.
             */
            double checksum = 0.0;

            for (int i = 0; i < warmups; i++) {
                Matrix result = a.multiply(b);
                checksum += result.get(0, 0);
            }

            double[] times = new double[runs];

            for (int i = 0; i < runs; i++) {

                long start = System.nanoTime();

                Matrix result = a.multiply(b);

                long end = System.nanoTime();

                checksum += result.get(0, 0);

                times[i] =
                    (end - start) / 1_000_000.0;

                System.out.printf(
                    "Run %d: %.3f ms%n",
                    i + 1,
                    times[i]
                );
            }

            double minimum =
                Arrays.stream(times).min().orElseThrow();

            double maximum =
                Arrays.stream(times).max().orElseThrow();

            double average =
                Arrays.stream(times).average().orElseThrow();

            double median =
                median(times);

            System.out.println();

            System.out.printf(
                "Minimum: %.3f ms%n",
                minimum
            );

            System.out.printf(
                "Median:  %.3f ms%n",
                median
            );

            System.out.printf(
                "Average: %.3f ms%n",
                average
            );

            System.out.printf(
                "Maximum: %.3f ms%n",
                maximum
            );

            // Prevent checksum from being completely unused.
            if (Double.isNaN(checksum)) {
                System.out.println(checksum);
            }

        } catch (IllegalArgumentException e) {

            System.err.println(
                "Input error: " + e.getMessage()
            );

            System.exit(1);

        } catch (OutOfMemoryError e) {

            System.err.println(
                "The requested matrices are too large"
                    + " for available memory."
            );

            System.exit(1);
        }
    }
}