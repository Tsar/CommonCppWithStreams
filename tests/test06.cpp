int main() {
    OStream s = OutputStream();

    int a = 27;
    int b = 10;
    a = a / b;
    s << a;

    a = 27;
    a /= b;
    s << a;

    a = 27;
    a %= b;
    s << a;

    a = -27;
    a = a % b;
    s << a;

    a = -27;
    a /= b;
    s << a;

    a = -27;
    a /= -10;
    s << a;

    a = -27;
    a %= -10;
    s << a;

    return 0;
}
