int f(int x) {
    {
        int a = 10;
        a * a;
    }
    return x * x;
}

int main() {
    int a = f(3);
    return 2 * a;
}
