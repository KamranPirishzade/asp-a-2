import sys
import time
import statistics

import numpy as np


def positive_integer(value, name):
    try:
        number = int(value)
    except ValueError:
        raise ValueError(f"{name} must be an integer.")

    if number <= 0:
        raise ValueError(f"{name} must be positive.")

    return number


def generate_matrix(rows, cols):
    row_indices = np.arange(
        rows,
        dtype=np.float64
    ).reshape(-1, 1)

    col_indices = np.arange(
        cols,
        dtype=np.float64
    ).reshape(1, -1)

    return (
        (row_indices * 31 + col_indices * 17) % 1000
    ) / 1000.0


def multiply(a, b):
    if a.ndim != 2 or b.ndim != 2:
        raise ValueError(
            "Both inputs must be two-dimensional matrices."
        )

    if a.shape[1] != b.shape[0]:
        raise ValueError(
            f"Incompatible dimensions: "
            f"{a.shape} and {b.shape}"
        )

    return a @ b


def main():
    if len(sys.argv) != 6:
        print(
            "Usage:",
            "python3 numpy_benchmark.py",
            "<rowsA> <shared> <colsB>",
            "<warmups> <runs>",
            file=sys.stderr
        )

        sys.exit(1)

    try:
        rows_a = positive_integer(
            sys.argv[1],
            "rowsA"
        )

        shared = positive_integer(
            sys.argv[2],
            "shared"
        )

        cols_b = positive_integer(
            sys.argv[3],
            "colsB"
        )

        warmups = positive_integer(
            sys.argv[4],
            "warmups"
        )

        runs = positive_integer(
            sys.argv[5],
            "runs"
        )

        a = generate_matrix(
            rows_a,
            shared
        )

        b = generate_matrix(
            shared,
            cols_b
        )

        print(f"A: {rows_a} x {shared}")
        print(f"B: {shared} x {cols_b}")
        print(f"Warm-ups: {warmups}")
        print(f"Measured runs: {runs}")
        print()

        checksum = 0.0

        # Warm-up phase
        for _ in range(warmups):
            result = multiply(a, b)
            checksum += result[0, 0]

        times = []

        for run in range(runs):

            start = time.perf_counter_ns()

            result = multiply(a, b)

            end = time.perf_counter_ns()

            checksum += result[0, 0]

            elapsed_ms = (
                end - start
            ) / 1_000_000

            times.append(elapsed_ms)

            print(
                f"Run {run + 1}: "
                f"{elapsed_ms:.3f} ms"
            )

        print()

        print(
            f"Minimum: {min(times):.3f} ms"
        )

        print(
            f"Median:  "
            f"{statistics.median(times):.3f} ms"
        )

        print(
            f"Average: "
            f"{statistics.mean(times):.3f} ms"
        )

        print(
            f"Maximum: {max(times):.3f} ms"
        )

        if np.isnan(checksum):
            print(checksum)

    except ValueError as error:
        print(
            f"Input error: {error}",
            file=sys.stderr
        )

        sys.exit(1)

    except MemoryError:
        print(
            "The requested matrices are too large "
            "for available memory.",
            file=sys.stderr
        )

        sys.exit(1)


if __name__ == "__main__":
    main()