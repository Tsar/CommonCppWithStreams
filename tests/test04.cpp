int f(int x) {
    x * f(x - 1);
}

int main() {
    int a;
    int b = f(1) * f(4);
    a = f(2) * f(3);
}
