
import numpy as np
from PIL import Image


def parse_slice(text):
    parts = text.strip().split(":")
    if len(parts) < 2 or len(parts) > 3:
        raise ValueError(f"'{text}' must look like start:stop or start:stop:step")
    values = []
    for part in parts:
        part = part.strip()
        if part == "":
            values.append(None)
        else:
            try:
                values.append(int(part))
            except ValueError:
                raise ValueError(f"'{part}' is not an integer")
                
    while len(values) < 3:
        values.append(None)
    if values[2] == 0:
        raise ValueError("step cannot be 0")
    return slice(values[0], values[1], values[2])


def main():
    path = input("Input image path: ").strip()
    try:
        image = np.array(Image.open(path).convert("RGB"))
    except (FileNotFoundError, OSError) as error:
        print("Cannot read image:", error)
        return
    print(f"Image size: {image.shape[0]} rows x {image.shape[1]} columns")

    try:
        rows = parse_slice(input("Row slice (start:stop:step): "))
        cols = parse_slice(input("Column slice (start:stop:step): "))
    except ValueError as error:
        print("Invalid slice:", error)
        return

    result = image[rows, cols]
    if result.shape[0] == 0 or result.shape[1] == 0:
        print("The slice is empty, nothing to save.")
        return

    output = input("Output file (.png): ").strip()
    Image.fromarray(result).save(output)
    print(f"Saved {result.shape[0]} x {result.shape[1]} image to {output}")


if __name__ == "__main__":
    main()