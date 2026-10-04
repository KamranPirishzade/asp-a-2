# TASK 2 - Memory size of tuple vs list in Python


## 1. Problem
A tuple and a list can hold the same data, e.g. `(1, 2, 3)` and `[1, 2, 3]`, but `__sizeof__()` reports different sizes for them. The goal of this report is to measure these sizes and explain what each structure stores actually in memory, and which design decisions cause the difference.

## 2. Environment
- Python 3.14.7 (CPython)
- macOS, Apple M5, 64-bit

This matters because a pointer is 8 bytes on a 64-bit system, and CPython's
internal object layout changes between versions (see 5.2).

## 3. Method
All experiments are in [`experiment.py`](experiment.py).

| # | Experiment | Code | Purpose |
|---|---|---|---|
| 1 | Given code from the task | `(1,2,3).__sizeof__()`, `[1,2,3].__sizeof__()` | Baseline |
| 2 | Empty tuple and empty list | `().__sizeof__()`, `[].__sizeof__()` | Size of the container alone |
| 3 | Tuples of length 0-10 | `tuple(range(n)).__sizeof__()` in a loop | To find the cost per element |
| 4 | Append 20 items to an empty list | `lst.append(i)`, print size only when it changes | Does a list grow smoothly or in steps? |
| 5 | `__sizeof__()` vs `sys.getsizeof()` | `sys.getsizeof(x)` vs `x.__sizeof__()` | Difference between the two methods |
| 6 | List created in 3 ways | `[1,2,3]`, `list((1,2,3))`, `[x for x in range(1, 4)]` | Does the creation method change the memory size? |

## 4. Results

**Experiments 1, 2, 6**

| Object | `__sizeof__()` (bytes) |
|---|---|
| `()` | 32 |
| `[]` | 40 |
| `(1, 2, 3)` | 56 |
| `[1, 2, 3]` (literal) | 72 |
| `list((1, 2, 3))` | 72 |
| `[x for x in range(1, 4)]` | 72 |

**Experiment 3: tuples**

| Length | 0 | 1 | 2 | 3 | 4 | 5 | 10 |
|---|---|---|---|---|---|---|---|
| Size | 32 | 40 | 48 | 56 | 64 | 72 | 112 |

**Experiment 4: list while appending** (size printed only when it changes)

| Length | Size | Reserved slots = (size - 40) / 8 |
|---|---|---|
| 0 | 40 | 0 |
| 1 | 72 | 4 |
| 5 | 104 | 8 |
| 9 | 168 | 16 |
| 17 | 232 | 24 |

**Experiment 5**

| Object | `__sizeof__()` | `sys.getsizeof()` | Difference |
|---|---|---|---|
| `(1, 2, 3)` | 56 | 72 | 16 |
| list from exp. 4 (20 items) | 232 | 248 | 16 |

## 5. Analysis

### 5.1 Tuples grow by exactly 8 bytes per element
**Observation:** Each extra element adds exactly 8 bytes (32 -> 112 bytes for 0 -> 10 elements).
**Investigation:** I calculated the difference between consecutive sizes; it is always 8.
**Explanation:** A tuple does not store the values themselves, only a pointer to each
object. A pointer is 8 bytes on 64-bit, so size = header + 8*n. Since a tuple is
immutable, Python allocates exactly n slots and no empty room for new elements.

### 5.2 The empty tuple is 32 bytes, not 24
**Observation:** Many sources give 24 bytes for an empty tuple, and in Python 3.13 it is 24 bytes (see the 3.13 tuple header in the references), but I measured 32.
**Investigation:** The classic tuple header has three 8-byte fields: reference count,
type pointer and length (24 bytes). I checked the CPython source for 3.14.
**Explanation:** Python 3.14 added an `ob_hash` field that caches the tuple's hash
(CPython issue gh-131525). Tuples are often used as dictionary keys, so storing the hash once avoids recomputing it. The cost is 8 extra bytes per tuple: a trade of memory for speed.

### 5.3 Lists grow in jumps
**Observation:** While appending, the size changes only at lengths 1, 5, 9 and 17.
Reserved slots go 0 -> 4 -> 8 -> 16 -> 24.
**Investigation:** I compared my data with the resize rule in CPython's
`list_resize()` (`Objects/listobject.c`), which is approximately
`new_allocated = (newsize + newsize // 8 + 6)` rounded down to a multiple of 4.
The comment in `list_resize()` lists the growth pattern 0, 4, 8, 16, 24..., which matches my measurements.
**Explanation:** If a list grew by one slot per `append`, every append would need a new memory block and a copy of all items (O(n)). Instead CPython over-allocates, so most appends just fill a free slot. This makes `append` amortized O(1). The price is unused memory: at length 17 there are 24 slots, 7 of them empty.

### 5.4 `[1, 2, 3]` is 72 bytes, not 64
**Observation:** With the tuple logic, 3 items should cost 40 + 3*8 = 64 bytes. The measured 72 bytes means 4 slots.
**Investigation:** My hypothesis was that the creation method causes this, so I
tested a literal, `list()` and a list comprehension. All three gave 72, so the
hypothesis was wrong.
**Explanation:** In CPython 3.14 all three creation paths leave empty capacity for a
small list. A list is never an exact fit here, while a tuple always is.

### 5.5 Empty list (40) is bigger than empty tuple (32)
**Observation:** An empty list is 8 bytes bigger than an empty tuple.
**Explanation:** A list has the basic fields (reference count, type, length) plus:
- a pointer to a separate array of items - the array must be replaceable when the
  list grows, while a tuple stores its item pointers inline after the header;
- the allocated capacity - how many slots are reserved, not only used.

That is 5 * 8 = 40 bytes. Both extra fields exist because a list is mutable.

### 5.6 `getsizeof` is 16 bytes larger
**Observation:** `sys.getsizeof()` is 16 bytes more than `__sizeof__()` for both objects.
**Explanation:** `getsizeof` adds the garbage collector header. Containers can form
reference cycles (e.g. a list containing itself), which reference counting cannot
free, so CPython tracks them in its cycle collector. Note: the list measured in
experiment 5 is the 20-item list from experiment 4; the 16-byte difference does not
depend on length.

## 6. Critique and limitations

**Different design goals.** A list pays memory for speed: spare slots make `append`
fast, but part of its capacity is empty. A tuple is compact and exact but cannot
change. Neither is better in general they optimize for different uses.

**`__sizeof__` is shallow.** It counts only the container (header + pointers), not
the elements. A tuple of three very large strings reports the same 56 bytes as
`(1, 2, 3)`. To measure real memory use, elements must be counted recursively or a
tool like `tracemalloc` must be used.

**Results are not universal.** All numbers are for CPython 3.14 on 64-bit. The empty
tuple (32 here, 24 in older versions) shows that the layout changes between versions.
On 32-bit, pointers are 4 bytes, and other implementations (e.g. PyPy) use a
different memory model. These sizes should never be hard-coded in program logic.

**Practical conclusion.** For a few objects the difference does not matter. For
millions of small fixed records (e.g. coordinates), tuples save the extra header
fields and the spare capacity in every record. Tuples also show intent: the data
does not change. A list is the right choice only when the size really needs to change.

## 7. How to run
```
python3 task2/experiment.py
```
## References
- [Python docs: `sys.getsizeof`](https://docs.python.org/3/library/sys.html#sys.getsizeof), GC overhead added by getsizeof
- [Python Design FAQ: How are lists implemented in CPython?](https://docs.python.org/3/faq/design.html#how-are-lists-implemented-in-cpython), over-allocation
- [CPython issue gh-131525](https://github.com/python/cpython/issues/131525), tuple hash caching in Python 3.14
- [CPython `Objects/listobject.c`](https://github.com/python/cpython/blob/main/Objects/listobject.c), `list_resize()` growth formula
- [CPython 3.13 `Include/cpython/tupleobject.h`](https://github.com/python/cpython/blob/3.13/Include/cpython/tupleobject.h), tuple header before 3.14

