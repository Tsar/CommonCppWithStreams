int main() {
    int a, b;
    IStream is = InputFileStream("abcde.txt");
    is >> a >> b;
    OStream os = OutputStream();
    os << a * 2 << a <<< a;
    return 0;
}
