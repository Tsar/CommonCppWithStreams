int main() {
    OStream s = OutputBinaryFileStream("test00_output.txt");
    int a = 0, b = 1, c = 2;
    a = (a + b * (c - a) / 2) - (a - b);
    s << 1819043144 << 1998597231 << 1684828783 << 169943329;
    s = OutputStream();
    s << a << b << c;
    return 0;
}
