int c = 3456;

int main() {
    IStream is = InputFileStream("number.txt");
    //is = InputStream();
    OStream os = OutputStream();

    int a, b;
    is >> a;
    is >> b >> c;

    os << a << b << c;

    is = InputBinaryFileStream("test00_output.txt");
    int d;
    is >> a >> b; is >> c; is >> d;

    os << a << b << c << d;
    os << 1819043144 << 1998597231 << 1684828783 << 169943329;

    return 0;
}
