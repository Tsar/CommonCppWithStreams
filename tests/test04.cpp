int f(int x) {
    if (x == 0) {
        int b;
        return 1;
    }
    return x * f(x - 1);
}

int main() {
    return f(5);
}
