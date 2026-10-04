# TASK 1 - Endianness

## 1. What is endianness

A 32-bit number like `0xa1b2c3d4` uses 4 bytes in memory. The computer has to decide in which order these bytes will be stored. This order is called endianness.

| Address | 1000 | 1001 | 1002 | 1003 |
|---|---|---|---|---|
| Big endian | a1 | b2 | c3 | d4 |
| Little endian | d4 | c3 | b2 | a1 |

- **Big endian** stores the most significant byte (the one with the largest value, `a1`) first, similar to how we normally write numbers.
- **Little endian** stores the least significant byte (`d4`) first, so the byte order looks reversed in memory.

The names come from *Gulliver's Travels*, where two groups argue about which side of an egg should be broken first. Danny Cohen used these names in 1980 for byte order in computers.

## 2. Experiment

**Environment:** macOS, Apple M5 (arm64), Apple clang 21.0.0, used through the `gcc` command.

I chose C even though I normally use JavaScript or Python more, because C lets me look directly at the bytes of a variable in memory by using a byte pointer. In Python and JavaScript, memory details are mostly hidden by the runtime.

The program reads a 32-bit hex number, prints its bytes as stored in memory, converts it to network byte order with `htonl`, then shows the value read without and with `ntohl`.

Output for `a1b2c3d4`:

```text
Stored in memory:           d4 c3 b2 a1
After htonl (network):      a1 b2 c3 d4
Wrong (no ntohl):           0xd4c3b2a1
Correct (with ntohl):       0xa1b2c3d4
```

| Input | Result | What it shows |
|---|---|---|
| `1` | memory: `01 00 00 00`, wrong read: `0x01000000` | the smallest byte is stored first; 1 becomes 16,777,216 if read in the wrong order |
| `xyz` | `Invalid input` | the program rejects invalid input |

**Observations:** The result `d4 c3 b2 a1` shows that my machine uses little endian, which matches the AArch64 little-endian rule (Burns, slide 22).

## 3. Where it matters

Endianness matters when binary data is moved between systems or stored in a binary format.

- **Network protocols:** IP and TCP use big endian, also called network byte order. This is why functions like `htonl` and `ntohl` are needed.
- **Binary files:** If a file stores numbers as raw bytes, the file format should define which byte order is used.

Text, such as HTTP headers, is read byte by byte, so byte order does not matter there. High-level languages like JavaScript and Python also hide it from the developer.

## 4. Critique

**Neither byte order is really better.** Little endian stores the least significant byte first, so with the number `1` the first byte in memory is `01`. Because of this, reading 1, 2 or 4 bytes from the same address gives the same small value, which made some low-level operations simpler in early CPUs. Big endian is easier for humans to read, because the bytes appear in the same order as the number is written. The more important thing is that both systems agree on the same order.

**Both exist mostly because of history.** Different CPU manufacturers made different choices in the past, and network protocols later standardized on big endian. Today most common computers use little endian but networks still use big endian, so conversion is still needed when sending and receiving binary data.

**The bug can be difficult to notice.** If the conversion is forgotten, the program does not crash, it simply gives a wrong number. In my test, the value `1` was read as `0x01000000` (16,777,216 in decimal), which is still a valid number, so this type of bug may not be obvious. For me, the main lesson is that the exact choice is less important than both sides using the same order. Formats that define their byte order explicitly are safer than code that relies on the machine's order.

**Limitations of my experiment:** I tested only on my own machine, which uses little endian, not on a real big endian machine. Also, the input validation with `scanf` is not perfect: input like `a1zz` is partly accepted as `a1`, so the program could have stronger input checking.

## 5. How to run

```bash
gcc -Wall -Wextra -o task1/endianness task1/endianness.c
./task1/endianness
```

## References

- [Big Endian vs Little Endian, And What To Do About It](https://www.youtube.com/watch?v=QkEtQ6jMsp8)
- [D. Cohen, "On Holy Wars and a Plea for Peace", IEN 137, 1980](https://www.rfc-editor.org/ien/ien137.txt)
- [RFC 791, Internet Protocol, Appendix B: Data Transmission Order](https://www.rfc-editor.org/rfc/rfc791)
- [J. Burns, CSCI 6461 Computer Systems Architecture, Topic 1, slide 22: Endianness](https://github.com/jzburns/csci-comp-arch/blob/master/latex/csci-6461/csci-6461-1.pdf)