public final class Matrix {
    private final int rows;
    private final int cols;
    private final double[] data;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException(
                "Rows and columns must be positive."
            );
        }

        long elementCount = (long) rows * cols;

        if (elementCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Matrix is too large for this implementation."
            );
        }

        this.rows = rows;
        this.cols = cols;
        this.data = new double[(int) elementCount];
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public double get(int row, int col) {
        checkIndexes(row, col);
        return data[row * cols + col];
    }

    public void set(int row, int col, double value) {
        checkIndexes(row, col);
        data[row * cols + col] = value;
    }

    private void checkIndexes(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(
                "Invalid index: (" + row + ", " + col + ")"
            );
        }
    }

    /*
     * Creates data programmatically.
     * No matrix values or dimensions are hard-coded.
     */
    public static Matrix generate(int rows, int cols) {
        Matrix matrix = new Matrix(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.data[i * cols + j] =
                    ((i * 31L + j * 17L) % 1000) / 1000.0;
            }
        }

        return matrix;
    }

    /*
     * Matrix multiplication using i-k-j loop order.
     */
    public Matrix multiply(Matrix other) {
        if (other == null) {
            throw new IllegalArgumentException("Other matrix cannot be null.");
        }

        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                "Cannot multiply "
                    + this.rows + "x" + this.cols
                    + " by "
                    + other.rows + "x" + other.cols
            );
        }

        Matrix result = new Matrix(this.rows, other.cols);

        int aCols = this.cols;
        int bCols = other.cols;

        for (int i = 0; i < this.rows; i++) {
            int aRow = i * aCols;
            int resultRow = i * bCols;

            for (int k = 0; k < aCols; k++) {
                double aValue = this.data[aRow + k];
                int bRow = k * bCols;

                for (int j = 0; j < bCols; j++) {
                    result.data[resultRow + j] +=
                        aValue * other.data[bRow + j];
                }
            }
        }

        return result;
    }
}