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