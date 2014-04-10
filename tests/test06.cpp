int main() {
    int a = 0, b = 0, c = 0;
    int x = (a = b)++;
    printf("a = %d; b = %d; c = %d; x = %d\n", a, b, c, x);
    return 0;
}
