public final class MatrixTest {

    private static final double EPSILON = 1e-9;

    private static Matrix referenceMultiply(Matrix a, Matrix b) {
        if (a.cols() != b.rows()) {
            throw new IllegalArgumentException(
                "Incompatible matrix dimensions."
            );
        }

        Matrix result = new Matrix(a.rows(), b.cols());

        // Simple i-j-k implementation used only as a correctness reference.
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < b.cols(); j++) {

                double sum = 0.0;

                for (int k = 0; k < a.cols(); k++) {
                    sum += a.get(i, k) * b.get(k, j);
                }

                result.set(i, j, sum);
            }
        }

        return result;
    }

    private static void assertMatricesEqual(
        Matrix expected,
        Matrix actual
    ) {
        if (expected.rows() != actual.rows()
            || expected.cols() != actual.cols()) {

            throw new AssertionError(
                "Matrix dimensions are different."
            );
        }

        for (int i = 0; i < expected.rows(); i++) {
            for (int j = 0; j < expected.cols(); j++) {

                double expectedValue = expected.get(i, j);
                double actualValue = actual.get(i, j);

                if (Math.abs(expectedValue - actualValue) > EPSILON) {
                    throw new AssertionError(
                        "Mismatch at (" + i + ", " + j + "): "
                            + "expected " + expectedValue
                            + ", got " + actualValue
                    );
                }
            }
        }
    }

    private static void testMultiplication(
        int rowsA,
        int shared,
        int colsB
    ) {
        Matrix a = Matrix.generate(rowsA, shared);
        Matrix b = Matrix.generate(shared, colsB);

        Matrix expected = referenceMultiply(a, b);
        Matrix actual = a.multiply(b);

        assertMatricesEqual(expected, actual);

        System.out.println("PASS: general multiplication");
    }

    private static void testIdentity(int size) {
        Matrix a = Matrix.generate(size, size);
        Matrix identity = new Matrix(size, size);

        for (int i = 0; i < size; i++) {
            identity.set(i, i, 1.0);
        }

        Matrix result = a.multiply(identity);

        assertMatricesEqual(a, result);

        System.out.println("PASS: identity matrix");
    }

    private static void testInvalidDimensions(
        int rowsA,
        int shared,
        int colsB
    ) {
        Matrix a = new Matrix(rowsA, shared);

        Matrix incompatible =
            new Matrix(shared + 1, colsB);

        boolean exceptionThrown = false;

        try {
            a.multiply(incompatible);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }

        if (!exceptionThrown) {
            throw new AssertionError(
                "Expected incompatible dimensions to be rejected."
            );
        }

        System.out.println("PASS: invalid dimensions rejected");
    }

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

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println(
                "Usage: java MatrixTest <rowsA> <shared> <colsB>"
            );

            System.err.println(
                "Example: java MatrixTest 4 5 6"
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

            testMultiplication(rowsA, shared, colsB);

            int identitySize = Math.min(
                Math.min(rowsA, shared),
                colsB
            );

            testIdentity(identitySize);

            testInvalidDimensions(
                rowsA,
                shared,
                colsB
            );

            System.out.println();
            System.out.println("All tests passed.");

        } catch (IllegalArgumentException e) {
            System.err.println(
                "Input error: " + e.getMessage()
            );

            System.exit(1);
        }
    }
}