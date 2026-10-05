import numpy as np
from PIL import Image
import sys


def parse_slice(text):
    """Parse start:stop:step without using eval()."""
    text = text.strip()

    parts = text.split(":")

    if len(parts) < 2 or len(parts) > 3:
        raise ValueError(
            "Slice must have the form start:stop or start:stop:step"
        )

    while len(parts) < 3:
        parts.append("")

    values = []

    for part in parts:
        part = part.strip()

        if part == "":
            values.append(None)
        else:
            try:
                values.append(int(part))
            except ValueError:
                raise ValueError(
                    f"Invalid slice value: {part!r}. "
                    "Only integers or empty fields are allowed."
                )

    start, stop, step = values

    if step == 0:
        raise ValueError("Slice step cannot be zero.")

    return slice(start, stop, step)


def normalize_to_uint8(matrix):
    """
    Convert matrix values into exactly defined grayscale values.
    Both Python and Java use the same formula.
    """
    if matrix.size == 0:
        return np.empty(matrix.shape, dtype=np.uint8)

    minimum = int(matrix.min())
    maximum = int(matrix.max())

    if minimum == maximum:
        return np.zeros(matrix.shape, dtype=np.uint8)

    # Integer arithmetic, matching Java exactly.
    result = (
        (matrix.astype(np.int64) - minimum) * 255
        // (maximum - minimum)
    )

    return result.astype(np.uint8)


def save_image(matrix, filename, scale=100):
    if matrix.size == 0:
        print("Slice is empty, so no image was created.")
        return

    gray = normalize_to_uint8(matrix)

    # Repeat pixels instead of relying on an image resizing implementation.
    scaled = np.repeat(
        np.repeat(gray, scale, axis=0),
        scale,
        axis=1
    )

    image = Image.fromarray(scaled, mode="L")
    image.save(filename)


def main():
    try:
        rows = int(input("Number of rows: "))
        cols = int(input("Number of columns: "))

        if rows <= 0 or cols <= 0:
            raise ValueError("Rows and columns must be positive.")

        print(f"Enter {rows * cols} integer values:")

        values = []

        while len(values) < rows * cols:
            line = input()
            values.extend(map(int, line.split()))

        if len(values) != rows * cols:
            raise ValueError("Too many matrix values.")

        matrix = np.array(
            values,
            dtype=np.int64
        ).reshape(rows, cols)

        row_text = input(
            "Row slice (start:stop:step): "
        )

        col_text = input(
            "Column slice (start:stop:step): "
        )

        row_slice = parse_slice(row_text)
        col_slice = parse_slice(col_text)

        result = matrix[row_slice, col_slice]

        print("\nOriginal matrix:")
        print(matrix)

        print("\nResult:")
        print(result)

        print("Shape:", result.shape)

        # Save exact numerical result for automatic comparison.
        np.savetxt(
            "numpy_result.txt",
            result,
            fmt="%d"
        )

        save_image(
            result,
            "numpy_slice.png"
        )

        if result.size != 0:
            print("Image: numpy_slice.png")

        print("Data: numpy_result.txt")

    except (ValueError, EOFError) as error:
        print("Error:", error)
        sys.exit(1)


if __name__ == "__main__":
    main()