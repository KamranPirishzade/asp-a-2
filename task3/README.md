# TASK 3 - Matrix multiplication

## Measurements

Best of 5 runs. Java includes 3 warm-up runs before measuring. Square matrices n x n.

| Size | NumPy | Java | Java / NumPy |
|---|---|---|---|
| 100 | 0.010 ms | 0.237 ms | ~24x |
| 200 | 0.077 ms | 1.152 ms | ~15x |
| 400 | 0.468 ms | 7.075 ms | ~15x |
| 800 | 2.421 ms | 56.542 ms | ~23x |
| 1000 | 4.044 ms | 111.072 ms | ~27x |

Code size (`wc -l`): `matmul.py` 43, `Matrix.java` 60, `Main.java` 49, `MatrixTest.java` 56.

Unit tests: 5 passed. With `+` changed to `-` in `multiply`, 4 of 5 tests failed (the size check test still passed, because it tests a different part).


## ChatGPT comparison

Full session: [chatgpt.md](chatgpt.md). Its code: [chatgpt/](chatgpt/), copied without changes.

### How it solved the problem

With the original task as the prompt, ChatGPT used a flat row-major array in Java and `a @ b` in NumPy. But it hard-coded the sizes, used JUnit (an external library), used the slower i-j-k loop order, measured only one run, and gave no real measurements.

### How I led it to the required result

1. **Prompt 1:** the original task text.
2. **Prompt 2:** asked for user input, tests without libraries, a check of the loop order, and fairer timing.

After prompt 2 it fixed all of these and switched to i-k-j by itself. No third prompt was needed.

### Comparison (800 x 800)

| | Mine | ChatGPT |
|---|---|---|
| Loop order | i-k-j | i-k-j (after prompt 2) |
| Tests | hand-checked values | compared with a second implementation |
| Java time | 56.5 ms | 57.3 ms |
| NumPy time | 2.4 ms | 1.7 ms |

The Java times are almost the same because the final algorithm is the same. Its NumPy script is faster, probably because it has warm-up runs and mine does not.

ChatGPT knew the theory from the start, but only applied it after I asked. To get a good result, I had to know what to ask for.