int c = 3456;
OStream os;

bool func() {
    os = OutputStream();
    os << 123456789;
    return false;
    os << 987654321;
}

int main() {
    IStream is = InputFileStream("number.txt");
    is = InputStream();
    OStream os = OutputStream();

    int a, b;
    is >> a;
    is >> b >> c;

    bool x, y;
    is >> x >> y;
    os << x << y;
    is >> x >> y;
    os << x << y;

    os << a << b << c;

    is = InputBinaryFileStream("test00_output.txt");
    int d;
    is >> a >> b; is >> c; is >> d;

    os << a << b << c << d;
    os << 1819043144 << 1998597231 << 1684828783 << 169943329;

    if (true || func()) {}  // checking laziness of ||
    os << 5050505;
    if (func() || true) {}

    return 0;
}
