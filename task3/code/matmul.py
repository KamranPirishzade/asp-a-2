import time

import numpy as np

RUNS = 5

def read_positive_int(prompt):
    value = input(prompt).strip()
    if not value.isdigit() or int(value) <= 0:
        raise ValueError(f"'{value}' is not a positive integer")
    return int(value)

def main():
    try:
        n = read_positive_int("Rows of A: ")
        m = read_positive_int("Columns of A (= rows of B): ")
        p = read_positive_int("Columns of B: ")
    except ValueError as error:
        print("Invalid input:", error)
        return

    rng = np.random.default_rng()
    a = rng.random((n, m))
    b = rng.random((m, p))

    best = float("inf")
    for _ in range(RUNS):
        start = time.perf_counter()
        c = a @ b
        best = min(best, time.perf_counter() - start)

    print(f"Result shape: {c.shape[0]}x{c.shape[1]}")
    print(f"Best of {RUNS} runs: {best * 1000:.3f} ms")



if __name__ == "__main__":
    main()