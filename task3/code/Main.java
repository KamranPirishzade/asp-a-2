import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final int WARMUP_RUNS = 3;   
    private static final int MEASURED_RUNS = 5; 
    private static int readPositiveInt(Scanner in, String prompt) {
        System.out.print(prompt);
        if (!in.hasNextInt()) {
            throw new IllegalArgumentException("not an integer: " + in.next());
        }
        int value = in.nextInt();
        if (value <= 0) {
            throw new IllegalArgumentException("must be positive: " + value);
        }
        return value;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n, m, p;
        try {
            n = readPositiveInt(in, "Rows of A: ");
            m = readPositiveInt(in, "Columns of A (= rows of B): ");
            p = readPositiveInt(in, "Columns of B: ");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
            return;
        }

        Random rng = new Random();
        Matrix a = Matrix.random(n, m, rng);
        Matrix b = Matrix.random(m, p, rng);

        for (int r = 0; r < WARMUP_RUNS; r++) {
            a.multiply(b);
        }

        long best = Long.MAX_VALUE;
        Matrix c = null;
        for (int r = 0; r < MEASURED_RUNS; r++) {
            long start = System.nanoTime();
            c = a.multiply(b);
            best = Math.min(best, System.nanoTime() - start);
        }

        System.out.println("Result shape: " + c.rows() + "x" + c.cols());
        System.out.printf("Best of %d runs: %.3f ms%n", MEASURED_RUNS, best / 1e6);
    }
}