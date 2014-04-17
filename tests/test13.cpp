int main() {
    OStream s = OutputStream();
    int a = 4, b = 4, c = 5;
    if (a == b && b != c) {
        s << 1;
    }
    if (a == b && b == c) {
        s << 2;
    }
    if (a == b || b == c) {
        s << 3;
    }
    if (a != b || b == c) {
        s << 4;
    }
    if (a) {
        s << 5;
    }
    if (0) {
        s << 6;
    }
    return 0;
}
