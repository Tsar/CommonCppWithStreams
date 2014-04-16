int main() {
    int i = 0;
    for (i = 0; i != 15; ++i) {
        OStream s = OutputStream();
        s << i * 2;
    }
    return 0;
}
