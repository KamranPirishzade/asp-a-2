import sys
import numpy as np
from PIL import Image

first = input("First image: ").strip()
second = input("Second image: ").strip()

try:
    a = np.array(Image.open(first).convert("RGB"))
    b = np.array(Image.open(second).convert("RGB"))
except (FileNotFoundError, OSError) as error:
    print("Cannot read image:", error)
    sys.exit()

print("NumPy size:", a.shape)
print("Java size:", b.shape)

if a.shape != b.shape:
    print("Images have different sizes")
    sys.exit()

different = np.any(a != b, axis=2).sum()
total = a.shape[0] * a.shape[1]

print("Different pixels:", different, "of", total)

gap = np.full((a.shape[0], 20, 3), 255, dtype=np.uint8)
side_by_side = np.hstack((a, gap, b))

output = input("Output file: ").strip()
Image.fromarray(side_by_side).save(output)

print("Saved", output)