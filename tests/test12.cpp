int main() {
    int a;
    IStream is = InputFileStream("abc'de'.,&^!@#$%^&*()txt");
    is >> a;
    OStream os = OutputStream();
    os << a * 2;
    return 0;
}
