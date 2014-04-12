int f(int x) {
    if (x == 0)
        return 1;
    return x * f(x - 1);
}

int main() {
    int a;
    int b = f(1) * f(4);
    a = f(2) * f(3);
}
