#include <stdio.h>
#include <stdint.h>
#include <arpa/inet.h>

void print_bytes(const char *label, uint32_t value) {
    uint8_t *p = (uint8_t *)&value;
    printf("%-28s", label);
    for (size_t i = 0; i < sizeof(value); i++) {
        printf("%02x ", p[i]);
    }
    printf("\n");
}

int main(void) {
    uint32_t x;

    printf("Enter a 32-bit hex number (e.g. a1b2c3d4): ");
    if (scanf("%x", &x) != 1) {
        printf("Invalid input\n");
        return 1;
    }

    print_bytes("Stored in memory:", x);

    uint32_t net = htonl(x);
    print_bytes("After htonl (network):", net);

    printf("%-28s0x%08x\n", "Wrong (no ntohl):", net);

    printf("%-28s0x%08x\n", "Correct (with ntohl):", ntohl(net));

    return 0;
}