void main() {
    int a = 34567, b = -941215;
    OStream s = OutputStream();
    s << a << b;
    a ^= b ^= a ^= b;
    s << a << b;
}
