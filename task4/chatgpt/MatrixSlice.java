import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class MatrixSlice {

    public static int[][] slice(
            int[][] matrix,
            int rowStart,
            int rowEnd,
            int colStart,
            int colEnd) {

        int[][] result =
                new int[rowEnd - rowStart][colEnd - colStart];

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                result[i - rowStart][j - colStart] = matrix[i][j];
            }
        }

        return result;
    }

    public static void saveImage(int[][] matrix, String filename)
            throws Exception {

        int rows = matrix.length;
        int cols = matrix[0].length;

        int min = matrix[0][0];
        int max = matrix[0][0];

        for (int[] row : matrix) {
            for (int value : row) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }

        int scale = 100;

        BufferedImage image = new BufferedImage(
                cols * scale,
                rows * scale,
                BufferedImage.TYPE_BYTE_GRAY
        );

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                int gray;

                if (min == max) {
                    gray = 0;
                } else {
                    gray = (matrix[i][j] - min) * 255 / (max - min);
                }

                int rgb = (gray << 16) | (gray << 8) | gray;

                for (int y = i * scale; y < (i + 1) * scale; y++) {
                    for (int x = j * scale; x < (j + 1) * scale; x++) {
                        image.setRGB(x, y, rgb);
                    }
                }
            }
        }

        ImageIO.write(image, "png", new File(filename));
    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Number of rows: ");
        int rows = scanner.nextInt();

        System.out.print("Number of columns: ");
        int cols = scanner.nextInt();

        if (rows <= 0 || cols <= 0) {
            System.out.println("Rows and columns must be positive.");
            return;
        }

        int[][] matrix = new int[rows][cols];

        System.out.println(
                "Enter " + (rows * cols) + " matrix values:"
        );

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = scanner.nextInt();
            }
        }

        System.out.print("Start row: ");
        int rowStart = scanner.nextInt();

        System.out.print("End row (exclusive): ");
        int rowEnd = scanner.nextInt();

        System.out.print("Start column: ");
        int colStart = scanner.nextInt();

        System.out.print("End column (exclusive): ");
        int colEnd = scanner.nextInt();

        if (rowStart < 0 || rowStart >= rowEnd || rowEnd > rows ||
            colStart < 0 || colStart >= colEnd || colEnd > cols) {

            System.out.println("Invalid slice range.");
            return;
        }

        int[][] result = slice(
                matrix,
                rowStart,
                rowEnd,
                colStart,
                colEnd
        );

        System.out.println("\nSliced matrix:");

        for (int[] row : result) {
            for (int value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }

        saveImage(result, "java_slice.png");

        System.out.println("\nImage saved as java_slice.png");

        scanner.close();
    }
}