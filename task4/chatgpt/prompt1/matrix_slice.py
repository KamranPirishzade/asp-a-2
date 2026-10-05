import numpy as np
from PIL import Image

def main():
    rows = int(input("Number of rows: "))
    cols = int(input("Number of columns: "))

    if rows <= 0 or cols <= 0:
        print("Rows and columns must be positive.")
        return

    print(f"Enter {rows * cols} matrix values:")
    values = []

    while len(values) < rows * cols:
        values.extend(map(int, input().split()))

    if len(values) != rows * cols:
        print("Incorrect number of values.")
        return

    matrix = np.array(values).reshape(rows, cols)

    row_start = int(input("Start row: "))
    row_end = int(input("End row (exclusive): "))
    col_start = int(input("Start column: "))
    col_end = int(input("End column (exclusive): "))

    if not (0 <= row_start < row_end <= rows):
        print("Invalid row range.")
        return

    if not (0 <= col_start < col_end <= cols):
        print("Invalid column range.")
        return

    sliced = matrix[row_start:row_end, col_start:col_end]

    print("\nOriginal matrix:")
    print(matrix)

    print("\nSliced matrix:")
    print(sliced)

    # Convert values to grayscale for visualization.
    minimum = sliced.min()
    maximum = sliced.max()

    if minimum == maximum:
        image_data = np.zeros(sliced.shape, dtype=np.uint8)
    else:
        image_data = (
            (sliced - minimum) * 255 / (maximum - minimum)
        ).astype(np.uint8)

    # Scale up so individual matrix cells are visible.
    image = Image.fromarray(image_data, mode="L")
    image = image.resize(
        (sliced.shape[1] * 100, sliced.shape[0] * 100),
        Image.Resampling.NEAREST
    )

    image.save("numpy_slice.png")
    print("\nImage saved as numpy_slice.png")


if __name__ == "__main__":
    main()