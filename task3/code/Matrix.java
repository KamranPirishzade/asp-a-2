import java.util.Random;


public final class Matrix {
    private final int rows;
    private final int cols;
    private final double[] data;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive: " + rows + "x" + cols);
        }
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows * cols];
    }

    public static Matrix fromRows(double[][] values) {
        Matrix m = new Matrix(values.length, values[0].length);
        for (int i = 0; i < m.rows; i++) {
            if (values[i].length != m.cols) {
                throw new IllegalArgumentException("All rows must have the same length");
            }
            for (int j = 0; j < m.cols; j++) {
                m.set(i, j, values[i][j]);
            }
        }
        return m;
    }

    public static Matrix random(int rows, int cols, Random rng) {
        Matrix m = new Matrix(rows, cols);
        for (int k = 0; k < m.data.length; k++) {
            m.data[k] = rng.nextDouble();
        }
        return m;
    }

    public int rows() { return rows; }
    public int cols() { return cols; }

    public double get(int i, int j) { return data[i * cols + j]; }
    public void set(int i, int j, double value) { data[i * cols + j] = value; }

    public Matrix multiply(Matrix other) {
        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                "Cannot multiply " + rows + "x" + cols + " by " + other.rows + "x" + other.cols);
        }
        Matrix result = new Matrix(this.rows, other.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int k = 0; k < this.cols; k++) {
                double aik = this.get(i, k);
                for (int j = 0; j < other.cols; j++) {
                    result.set(i, j, result.get(i, j) + aik * other.get(k, j));
                }
            }
        }
        return result;
    }
}