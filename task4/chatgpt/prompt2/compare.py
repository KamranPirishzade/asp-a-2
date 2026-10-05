import numpy as np
from PIL import Image
import sys


def load_result(path):
    rows = []

    with open(path, "r") as file:
        for line in file:
            line = line.strip()

            if line:
                rows.append(
                    [int(x) for x in line.split()]
                )

    return rows


def main():
    numpy_result = load_result("numpy_result.txt")
    java_result = load_result("java_result.txt")

    if numpy_result != java_result:
        print("FAIL: sliced matrices are different.")
        sys.exit(1)

    print("PASS: sliced matrices are exactly identical.")

    try:
        numpy_image = np.array(
            Image.open("numpy_slice.png").convert("L")
        )

        java_image = np.array(
            Image.open("java_slice.png").convert("L")
        )

    except FileNotFoundError:
        print(
            "No images to compare "
            "(the slice may be empty)."
        )
        return

    if numpy_image.shape != java_image.shape:
        print(
            "FAIL: image dimensions are different:",
            numpy_image.shape,
            java_image.shape
        )
        sys.exit(1)

    different = np.count_nonzero(
        numpy_image != java_image
    )

    print(
        f"Different pixels: {different} "
        f"of {numpy_image.size}"
    )

    if different == 0:
        print("PASS: images are pixel-for-pixel identical.")
    else:
        print("FAIL: images are different.")
        sys.exit(1)


if __name__ == "__main__":
    main()