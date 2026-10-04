# ChatGPT session - Task 3

New chat, no earlier context. Model: GPT-5.6 Sol. 

## Prompt 1 - the original task

> Implement matrix multiplication using numpy arrays and Java. Write unit tests for the Java implementation to make sure the multiplication is correct. Perform an analysis of the code size and execution time.

### Answer (summary)

- **NumPy:** `multiply_matrices(a, b)` with a shape check, returning `a @ b`. Example matrices hard-coded. Benchmark with `size = 500`, one warm-up, one measured run.
- **Java:** a `Matrix` class with a flat row-major `double[]`, `get`/`set`, and `multiply` with a dimension check. Loop order i-j-k:

```java
for (int i = 0; i < this.rows; i++) {
    for (int j = 0; j < other.cols; j++) {
        double sum = 0;
        for (int k = 0; k < this.cols; k++) {
            sum += this.get(i, k) * other.get(k, j);
        }
        result.set(i, j, sum);
    }
}
```

- **Tests:** JUnit 5, 4 tests: 2x3 * 3x2 known result, identity, 1x1, incompatible sizes. No run commands.
- **Benchmark:** `MatrixBenchmark` with `size = 500`, fixed seed 42, one warm-up, one measured run. Mentioned that JMH should be used for serious benchmarking.
- **Analysis:** O(n^3) explanation, NumPy faster because of native BLAS libraries. Code size given as estimates (~20 lines for multiply, ~70 for the class). The timing table contained "measure" instead of numbers, with a note not to invent results.
- The answer also showed an interactive visualization of row x column multiplication.

### Missing compared with the assignment rules

- Sizes and matrices hard-coded, no user input, no input validation
- JUnit needs an external library, no instructions to run
- i-j-k loop order reads B column by column
- One warm-up and one run is not a fair measurement
- No real measurements

## Prompt 2 - steering

> Please improve the solution:
> 1. Do not hard-code any matrices or sizes. All parameters, like the matrix sizes, must be provided by user input, and the code must work for any size. Validate the input.
> 2. The code must be easy to compile and run. Write the unit tests without JUnit or any external library, and give the exact commands to run everything.
> 3. Is your loop order efficient for a row-major array? Improve it if needed and explain why.
> 4. One warm-up and one measured run is not a fair measurement. Improve the timing in both Java and NumPy.

### Answer (summary)

- **Loop order:** switched to i-k-j and explained it: B and C are read along rows, and A[i][k] is reused in the inner loop. It said that both orders are O(n^3), and only data locality changes.

```java
for (int i = 0; i < this.rows; i++) {
    int aRow = i * aCols;
    int resultRow = i * bCols;
    for (int k = 0; k < aCols; k++) {
        double aValue = this.data[aRow + k];
        int bRow = k * bCols;
        for (int j = 0; j < bCols; j++) {
            result.data[resultRow + j] += aValue * other.data[bRow + j];
        }
    }
}
```

- **Input:** all sizes, warm-up count and run count as command-line arguments, validated (positive integers, overflow check for very large matrices).
- **Matrix values:** generated with a deterministic formula `((i * 31 + j * 17) % 1000) / 1000.0` instead of random values.
- **`get`/`set`:** now check index bounds; `multiply` accesses `data` directly.
- **Tests:** no JUnit. The sizes come from command-line arguments; the result of `multiply` is compared with a simple i-j-k reference implementation (`referenceMultiply`), plus an identity test and an incompatible sizes test. No test with hand-checked values.
- **Benchmark:** user-defined warm-ups and runs; prints every run plus min, median, average and max. Recommended the median for the report. Uses a checksum of the results so the work is not removed by the JIT.
- **NumPy:** `numpy_benchmark.py` with the same five arguments, warm-ups and repeated runs.
- **Commands:** `javac Matrix.java MatrixTest.java MatrixBenchmark.java`, `java MatrixTest 10 20 15`, `java MatrixBenchmark 500 500 500 5 10`, `python3 numpy_benchmark.py 500 500 500 5 10`.
- Still no measurements, again with a note not to invent numbers. Mentioned that this is not a rigorous microbenchmark and JMH would be the professional tool.

No third prompt was needed.

## Running ChatGPT's final code

The code was copied without changes into [`chatgpt/`](chatgpt/). Everything compiled and ran on the first try.

```
$ java MatrixTest 10 20 15
PASS: general multiplication
PASS: identity matrix
PASS: invalid dimensions rejected

All tests passed.

$ java MatrixBenchmark 800 800 800 3 5
Run 1: 57.272 ms
Run 2: 58.153 ms
Run 3: 77.728 ms
Run 4: 62.400 ms
Run 5: 60.143 ms

Minimum: 57.272 ms
Median:  60.143 ms
Average: 63.139 ms
Maximum: 77.728 ms

$ python numpy_benchmark.py 800 800 800 3 5
Run 1: 1.814 ms
Run 2: 1.793 ms
Run 3: 1.730 ms
Run 4: 1.739 ms
Run 5: 1.769 ms

Minimum: 1.730 ms
Median:  1.769 ms
Average: 1.769 ms
Maximum: 1.814 ms
```