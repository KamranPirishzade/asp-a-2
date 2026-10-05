import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class SliceImage {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.print("Input image path: ");
        String path = in.nextLine().trim();

        BufferedImage image;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Cannot read image");
            return;
        }

        if (image == null) {
            System.out.println("Cannot read image");
            return;
        }

        int rows = image.getHeight();
        int cols = image.getWidth();
        System.out.println("Image size: " + rows + " rows x " + cols + " columns");

        int[] pixels = image.getRGB(0, 0, cols, rows, null, 0, cols);

        int[] rowIndices;
        int[] colIndices;

        try {
            System.out.print("Row slice: ");
            rowIndices = Slice.parse(in.nextLine()).indices(rows);

            System.out.print("Column slice: ");
            colIndices = Slice.parse(in.nextLine()).indices(cols);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid slice: " + e.getMessage());
            return;
        }

        if (rowIndices.length == 0 || colIndices.length == 0) {
            System.out.println("Empty slice");
            return;
        }

        int outRows = rowIndices.length;
        int outCols = colIndices.length;
        int[] result = new int[outRows * outCols];

        for (int r = 0; r < outRows; r++) {
            int sourceRow = rowIndices[r] * cols;
            for (int c = 0; c < outCols; c++) {
                result[r * outCols + c] = pixels[sourceRow + colIndices[c]];
            }
        }

        BufferedImage output =
            new BufferedImage(outCols, outRows, BufferedImage.TYPE_INT_RGB);

        output.setRGB(0, 0, outCols, outRows, result, 0, outCols);

        System.out.print("Output file: ");
        String outPath = in.nextLine().trim();

        try {
            ImageIO.write(output, "png", new File(outPath));
        } catch (IOException e) {
            System.out.println("Cannot save image");
            return;
        }

        System.out.println("Saved " + outRows + " x " + outCols + " image to " + outPath);
    }
}