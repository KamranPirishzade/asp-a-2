import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;


public class MatrixSlice {

    static class Slice {
        Long start;
        Long stop;
        Long step;

        Slice(Long start, Long stop, Long step) {
            this.start = start;
            this.stop = stop;
            this.step = step;
        }
    }


    static class NormalizedSlice {
        long start;
        long stop;
        long step;
        int length;

        NormalizedSlice(long start, long stop, long step, int length) {
            this.start = start;
            this.stop = stop;
            this.step = step;
            this.length = length;
        }
    }


    static class Matrix {
        int rows;
        int cols;
        long[] data;

        Matrix(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            this.data = new long[Math.multiplyExact(rows, cols)];
        }

        long get(int row, int col) {
            return data[row * cols + col];
        }

        void set(int row, int col, long value) {
            data[row * cols + col] = value;
        }
    }


    static Slice parseSlice(String text) {
        text = text.trim();

        String[] parts = text.split(":", -1);

        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException(
                "Slice must have the form start:stop "
                + "or start:stop:step."
            );
        }

        Long start = parseOptionalLong(parts[0]);
        Long stop = parseOptionalLong(parts[1]);

        Long step = null;

        if (parts.length == 3) {
            step = parseOptionalLong(parts[2]);
        }

        if (step != null && step == 0) {
            throw new IllegalArgumentException(
                "Slice step cannot be zero."
            );
        }

        return new Slice(start, stop, step);
    }


    static Long parseOptionalLong(String text) {
        text = text.trim();

        if (text.isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid slice value: " + text
            );
        }
    }


    /*
     * Equivalent to Python's slice.indices(length).
     *
     * Missing start/stop values must be treated separately from
     * explicitly supplied negative values.
     */
    static NormalizedSlice normalizeSlice(Slice slice, int length) {

        long step = slice.step == null ? 1 : slice.step;

        if (step == 0) {
            throw new IllegalArgumentException(
                "Slice step cannot be zero."
            );
        }

        long start;
        long stop;

        if (step > 0) {

            if (slice.start == null) {
                start = 0;
            } else {
                start = slice.start;

                if (start < 0) {
                    start = safeAddLength(start, length);
                }

                if (start < 0) {
                    start = 0;
                } else if (start > length) {
                    start = length;
                }
            }

            if (slice.stop == null) {
                stop = length;
            } else {
                stop = slice.stop;

                if (stop < 0) {
                    stop = safeAddLength(stop, length);
                }

                if (stop < 0) {
                    stop = 0;
                } else if (stop > length) {
                    stop = length;
                }
            }

        } else {

            if (slice.start == null) {
                start = length - 1L;
            } else {
                start = slice.start;

                if (start < 0) {
                    start = safeAddLength(start, length);
                }

                if (start < 0) {
                    start = -1;
                } else if (start >= length) {
                    start = length - 1L;
                }
            }

            if (slice.stop == null) {
                stop = -1;
            } else {
                stop = slice.stop;

                if (stop < 0) {
                    stop = safeAddLength(stop, length);
                }

                if (stop < 0) {
                    stop = -1;
                } else if (stop >= length) {
                    stop = length - 1L;
                }
            }
        }

        long count;

        if (step > 0) {
            if (start >= stop) {
                count = 0;
            } else {
                count = 1 + (stop - 1 - start) / step;
            }
        } else {
            if (start <= stop) {
                count = 0;
            } else {
                // Avoid negating Long.MIN_VALUE.
                long distance = start - stop - 1;
                long positiveStep =
                    step == Long.MIN_VALUE ? Long.MAX_VALUE : -step;

                if (step == Long.MIN_VALUE) {
                    count = 1;
                } else {
                    count = 1 + distance / positiveStep;
                }
            }
        }

        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "Slice is too large."
            );
        }

        return new NormalizedSlice(
            start,
            stop,
            step,
            (int) count
        );
    }


    static long safeAddLength(long value, int length) {
        if (value < Long.MIN_VALUE + length) {
            return Long.MIN_VALUE;
        }

        return value + length;
    }


    static Matrix slice(
            Matrix source,
            NormalizedSlice rows,
            NormalizedSlice cols) {

        Matrix result = new Matrix(rows.length, cols.length);

        long sourceRow = rows.start;

        for (int r = 0; r < rows.length; r++) {

            long sourceCol = cols.start;
            int destinationBase = r * result.cols;
            int sourceBase = (int) sourceRow * source.cols;

            for (int c = 0; c < cols.length; c++) {

                result.data[destinationBase + c] =
                    source.data[sourceBase + (int) sourceCol];

                sourceCol += cols.step;
            }

            sourceRow += rows.step;
        }

        return result;
    }


    static byte[] grayscale(Matrix matrix) {

        if (matrix.data.length == 0) {
            return new byte[0];
        }

        long min = matrix.data[0];
        long max = matrix.data[0];

        for (long value : matrix.data) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        byte[] result = new byte[matrix.data.length];

        if (min == max) {
            return result;
        }

        /*
         * This simple integer formula is exact for ordinary assignment-sized
         * integer inputs. Subtract/multiply are checked so overflow is not
         * silently accepted.
         */
        long range = Math.subtractExact(max, min);

        for (int i = 0; i < matrix.data.length; i++) {

            long shifted = Math.subtractExact(matrix.data[i], min);
            long numerator = Math.multiplyExact(shifted, 255L);

            long gray = numerator / range;

            result[i] = (byte) gray;
        }

        return result;
    }


    static void saveImage(
            Matrix matrix,
            String filename,
            int scale) throws Exception {

        if (matrix.rows == 0 || matrix.cols == 0) {
            System.out.println(
                "Slice is empty, so no image was created."
            );
            return;
        }

        int width = Math.multiplyExact(matrix.cols, scale);
        int height = Math.multiplyExact(matrix.rows, scale);

        BufferedImage image = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_BYTE_GRAY
        );

        byte[] pixels =
            ((DataBufferByte) image.getRaster()
                                   .getDataBuffer())
                                   .getData();

        byte[] gray = grayscale(matrix);

        /*
         * Write directly to the image's backing byte array.
         * No setRGB() call for every pixel.
         */
        for (int row = 0; row < matrix.rows; row++) {

            int sourceOffset = row * matrix.cols;

            for (int sy = 0; sy < scale; sy++) {

                int outputOffset =
                    (row * scale + sy) * width;

                for (int col = 0; col < matrix.cols; col++) {

                    byte value = gray[sourceOffset + col];

                    Arrays.fill(
                        pixels,
                        outputOffset + col * scale,
                        outputOffset + (col + 1) * scale,
                        value
                    );
                }
            }
        }

        ImageIO.write(
            image,
            "png",
            new File(filename)
        );
    }


    static void saveResult(Matrix matrix, String filename)
            throws Exception {

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(filename))) {

            for (int r = 0; r < matrix.rows; r++) {

                for (int c = 0; c < matrix.cols; c++) {

                    if (c > 0) {
                        writer.write(" ");
                    }

                    writer.write(
                        Long.toString(matrix.get(r, c))
                    );
                }

                writer.newLine();
            }
        }
    }


    static void printMatrix(Matrix matrix) {

        if (matrix.rows == 0 || matrix.cols == 0) {
            System.out.println("[]");
            return;
        }

        for (int r = 0; r < matrix.rows; r++) {

            for (int c = 0; c < matrix.cols; c++) {
                System.out.print(matrix.get(r, c));

                if (c + 1 < matrix.cols) {
                    System.out.print("\t");
                }
            }

            System.out.println();
        }
    }


    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Number of rows: ");
            int rows = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Number of columns: ");
            int cols = Integer.parseInt(scanner.nextLine().trim());

            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException(
                    "Rows and columns must be positive."
                );
            }

            Matrix matrix = new Matrix(rows, cols);

            System.out.println(
                "Enter " + matrix.data.length
                + " integer values:"
            );

            for (int i = 0; i < matrix.data.length; i++) {
                if (!scanner.hasNextLong()) {
                    throw new IllegalArgumentException(
                        "Expected " + matrix.data.length
                        + " integer values."
                    );
                }

                matrix.data[i] = scanner.nextLong();
            }

            scanner.nextLine();

            System.out.print(
                "Row slice (start:stop:step): "
            );
            Slice rowSlice = parseSlice(scanner.nextLine());

            System.out.print(
                "Column slice (start:stop:step): "
            );
            Slice colSlice = parseSlice(scanner.nextLine());

            NormalizedSlice normalizedRows =
                normalizeSlice(rowSlice, rows);

            NormalizedSlice normalizedCols =
                normalizeSlice(colSlice, cols);

            Matrix result = slice(
                matrix,
                normalizedRows,
                normalizedCols
            );

            System.out.println("\nResult:");
            printMatrix(result);

            System.out.println(
                "Shape: (" + result.rows
                + ", " + result.cols + ")"
            );

            saveResult(result, "java_result.txt");

            saveImage(
                result,
                "java_slice.png",
                100
            );

            if (result.data.length != 0) {
                System.out.println(
                    "Image: java_slice.png"
                );
            }

            System.out.println(
                "Data: java_result.txt"
            );

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}