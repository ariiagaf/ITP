/*
 * A. Based Sum
 * Author: Arina Agafonova, CSE-03
 *
 * This program reads numbers in various bases (binary, octal, decimal, hexadecimal) from input.txt,
 * performs arithmetic operations on them based on their index (even-indexed: -10, odd-indexed: +10),
 * and writes the sum of the resulting numbers in decimal to output.txt.
 *
 * Input format (input.txt):
 * - First line: integer N (1 ≤ N ≤ 40) — number of numbers.
 * - Second line: N space-separated numbers (up to 6 digits each, digits allowed depend on base).
 * - Third line: N space-separated integers representing the bases of the numbers (2, 8, 10, or 16).
 *
 * Output format (output.txt):
 * - Sum of numbers after arithmetic operations in decimal, or "Invalid inputs" if input is invalid.
 */

#include <cstdio>
#include <cstdlib>
#include <cstring>

// Convert number in given base to decimal
int convertToDec(const char *number, int base) {
    return (int) strtol(number, nullptr, base);
}

// Validate if number conforms to its base
bool isValidNumber(const char *num, int base) {
    for (int i = 0; num[i] != '\0'; ++i) {
        char c = num[i];
        switch (base) {
            case 2:
                if (c != '0' && c != '1') return false;
                break;
            case 8:
                if (c < '0' || c > '7') return false;
                break;
            case 10:
                if (c < '0' || c > '9') return false;
                break;
            case 16:
                if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F'))) return false;
                break;
            default:
                return false;
        }
    }
    return true;
}

int main() {
    FILE *input = freopen("input.txt", "r", stdin);
    FILE *output = freopen("output.txt", "w", stdout);

    int N;
    fscanf(input, "%d", &N);

    // Validate number of inputs
    if (N < 1 || N > 40) {
        fprintf(output, "Invalid inputs\n");
        return 0;
    }

    char numbers[40][7]; // max 6 digits + null terminator
    int bases[40];

    // Read numbers
    for (int i = 0; i < N; ++i) {
        fscanf(input, "%6s", numbers[i]);
    }

    // Read bases and validate numbers
    for (int i = 0; i < N; ++i) {
        fscanf(input, "%d", &bases[i]);
        if (bases[i] != 2 && bases[i] != 8 && bases[i] != 10 && bases[i] != 16) {
            fprintf(output, "Invalid inputs\n");
            return 0;
        }
        if (!isValidNumber(numbers[i], bases[i])) {
            fprintf(output, "Invalid inputs\n");
            return 0;
        }
    }

    // Perform arithmetic operations and calculate sum
    int sum = 0;
    for (int i = 0; i < N; ++i) {
        int decimal = convertToDec(numbers[i], bases[i]);
        if (i % 2 == 0)
            decimal -= 10; // even index: subtract 10
        else
            decimal += 10; // odd index: add 10
        sum += decimal;
    }

    fprintf(output, "%d\n", sum);

    fclose(input);
    fclose(output);
    return 0;
}
