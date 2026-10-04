# TASK 3 - Matrix multiplication

## Measurements

Best of 5 runs. Java includes 3 warm-up runs before measuring.

| Input (n, m, p) | NumPy | Java |
|---|---|---|
| 2, 3, 4 | 0.001 ms | 0.002 ms |
| 300, 300, 300 | 0.176 ms | 3.455 ms |
| 1000, 1000, 1000 | 4.044 ms | 111.072 ms |

Invalid input is rejected in both versions (`abc`, `-1`).