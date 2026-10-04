public class MatrixTest {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static boolean same(Matrix a, Matrix b) {
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            return false;
        }
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.cols(); j++) {
                if (a.get(i, j) != b.get(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Matrix a = Matrix.fromRows(new double[][]{{1, 2}, {3, 4}});
        Matrix b = Matrix.fromRows(new double[][]{{5, 6}, {7, 8}});
        Matrix expected = Matrix.fromRows(new double[][]{{19, 22}, {43, 50}});
        check("2x2 known result", same(a.multiply(b), expected));

        Matrix identity = Matrix.fromRows(new double[][]{{1, 0}, {0, 1}});
        check("A * I = A", same(a.multiply(identity), a));

        Matrix buys = Matrix.fromRows(new double[][]{{2, 1, 3}, {0, 4, 1}});
        Matrix prices = Matrix.fromRows(new double[][]{{1, 2}, {3, 1}, {2, 2}});
        Matrix totals = Matrix.fromRows(new double[][]{{11, 11}, {14, 6}});
        check("2x3 * 3x2 (non-square)", same(buys.multiply(prices), totals));

        Matrix row = Matrix.fromRows(new double[][]{{1, 2, 3}});
        Matrix col = Matrix.fromRows(new double[][]{{4},{5},{6}});
        check("1x3 * 3x1 = 32", same(row.multiply(col), Matrix.fromRows(new double[][]{{32}})));

        boolean rejected = false;
        try {
            buys.multiply(a);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check("wrong sizes are rejected", rejected);

        System.out.println(passed + " passed, " + failed + " failed");
    }
}