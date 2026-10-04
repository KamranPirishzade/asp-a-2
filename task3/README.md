# TASK 3 - Matrix multiplication

## 1. Problem

The task is to implement matrix multiplication with NumPy and a C-like language. I chose Java. I also write unit tests for the Java version and compare code size and execution time.

## 2. Environment

- macOS, Apple M5
- Python 3.14.7, NumPy 2.5.3
- OpenJDK 27

## 3. Design decisions

**Java.** Its syntax is close to JavaScript, which I use more. I also need Java in Task 4.

**Sizes from user input.** The user enters `n`, `m` and `p`. A is `n x m`, B is `m x p`, and the result is `n x p`. The shared size `m` is entered once, so the matrices always fit. Text, zero and negative numbers are rejected.

**Random values.** The benchmark depends only on the size, so the values are random. The unit tests use fixed values with known results.

**One flat array in Java.** The matrix is one `double[]` in row-major order. Element `(i, j)` is at `i * cols + j`, the formula from the lecture slides. A `double[][]` would store separate rows in different places in memory.

**i-k-j loop order.** The result is the same as with `i-j-k`, but rows of B and of the result are read left to right. This fits row-major memory better.

**`get` and `set`.** They make the code easier to read. They are small, so the JIT compiler can inline them.

**Fair timing.** Only the multiplication is measured, 5 times, and I report the fastest run. Java first runs it 3 times without measuring, because the JIT optimizes the code while it runs.

## 4. Implementation

NumPy:

```python
c = a @ b
```

Java, in `Matrix.multiply`:

```java
for (int i = 0; i < this.rows; i++) {
    for (int k = 0; k < this.cols; k++) {
        double aik = this.get(i, k);
        for (int j = 0; j < other.cols; j++) {
            result.set(i, j, result.get(i, j) + aik * other.get(k, j));
        }
    }
}
```

For small matrices, `matmul.py` also prints A, B and C. I checked one result by hand:

```text
0.39 x 0.94 + 0.39 x 0.08 + 0.63 x 0.36 = 0.6246
```

NumPy shows `0.63`. The printed values are rounded to 2 decimals, but the calculation uses the full values.

## 5. Unit tests

The tests are in `MatrixTest.java` and use no external library.

| Test | What it checks |
|---|---|
| 2x2 known result | [[1,2],[3,4]] x [[5,6],[7,8]] = [[19,22],[43,50]] |
| A x I = A | the identity matrix does not change A |
| 2x3 x 3x2 | non-square matrices, result [[11,11],[14,6]] |
| 1x3 x 3x1 | edge case, the result is one value: 32 |
| wrong sizes | 2x3 times 2x2 is rejected |

```text
PASS  2x2 known result
PASS  A * I = A
PASS  2x3 * 3x2 (non-square)
PASS  1x3 * 3x1 = 32
PASS  wrong sizes are rejected
5 passed, 0 failed
```

To check that the tests catch mistakes, I changed `+` to `-` in `multiply`. Then 4 of 5 tests failed. The wrong-size test still passed, because it checks the size validation, not the calculation.

## 6. Analysis

### 6.1 Execution time

Square matrices `n x n`, best of 5 runs:

| Size | Multiply-add steps (n^3) | NumPy | Java | Java / NumPy |
|---|---|---|---|---|
| 100 | 1 million | 0.010 ms | 0.237 ms | ~24x |
| 200 | 8 million | 0.077 ms | 1.152 ms | ~15x |
| 400 | 64 million | 0.468 ms | 7.075 ms | ~15x |
| 800 | 512 million | 2.421 ms | 56.542 ms | ~23x |
| 1000 | 1 billion | 4.044 ms | 111.072 ms | ~27x |

**Growth.** Matrix multiplication needs about `n^3` steps, so doubling the size gives about 8 times more work. In Java, the time from 400 to 800 grew 8.0 times. For small sizes it grew less, because fixed costs like creating the result matrix matter more.

NumPy grew less than 8 times at every step. Its library becomes more efficient with bigger matrices, probably because it uses the cache and multiple cores better.

**Why NumPy is faster.** `a @ b` runs no Python loops. It calls an optimized native library that uses vector instructions, cache-friendly blocks and multiple cores. My Java code is a simple loop on one core. At 800 x 800, NumPy did about 210 billion steps per second, Java about 9 billion.

### 6.2 Code size

| File | Lines |
|---|---|
| `matmul.py` | 43 |
| `Matrix.java` | 60 |
| `Main.java` | 49 |
| `MatrixTest.java` | 56 |

Most lines are for input, validation, timing and tests. The multiplication itself is one expression in NumPy (`c = a @ b`) and about 15 lines in Java. NumPy is shorter, but the Java version shows how the algorithm works.

## 7. ChatGPT comparison

Full session: [chatgpt.md](chatgpt.md). Its code: [chatgpt/](chatgpt/), copied without changes.

### How it solved the problem

With the original task, ChatGPT used a flat row-major array in Java and `a @ b` in NumPy. But in the first answer it:

- hard-coded the matrix sizes,
- used JUnit, an external library,
- used the slower `i-j-k` loop order,
- measured only one run,
- gave no real benchmark results.

### How I led it to the required result

1. **Prompt 1:** the original task.
2. **Prompt 2:** asked for user input, no external libraries, better timing, and asked whether the loop order is efficient, without giving the answer.

After prompt 2 it fixed all of these and switched to `i-k-j` by itself. A third prompt was not needed.

### Comparison at 800 x 800

| | Mine | ChatGPT |
|---|---|---|
| Loop order | i-k-j | i-k-j after prompt 2 |
| Tests | fixed known values | compared with another implementation |
| Java time | 56.5 ms | 57.3 ms |
| NumPy time | 2.4 ms | 1.7 ms |

The Java times are close, because the final algorithm is almost the same. ChatGPT's NumPy version was faster, probably because it used warm-up runs and mine did not.

ChatGPT knew the right ideas, but its first answer was not complete. I still had to see what was missing and ask for it.

## 8. Critique and limitations

- **Java is not fully optimized.** It uses one core and no cache-friendly blocks. It shows the algorithm, but it cannot compete with NumPy.
- **The timing is simple.** Best of 5 runs reduces noise, but it is not a professional benchmark. For Java, JMH would be more reliable.
- **My NumPy version has no warm-up.** This may explain why ChatGPT's version was faster.
- **Only dense matrices of doubles** are supported.

## 9. How to run

NumPy, from the repository root:

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r task3/code/requirements.txt
python task3/code/matmul.py
```

Java, from `task3/code` (Java 22 or newer):

```bash
java Main.java
java MatrixTest.java
```

## References

- Lecture slides, Basics of Programming Languages, CSCI 6221: Accessing Multi-dimensioned Arrays, and Locating an Element in a Multi-Dimensioned Array (row-major order)
- [NumPy documentation: numpy.matmul](https://numpy.org/doc/stable/reference/generated/numpy.matmul.html)