bool abc = false;
bool e = true;

int main()
{
    bool t = true;

    OStream s = OutputStream();
    s << t << false << true;
    int a = 15;
    s << abc << e << (2 == 2);
    s << 0 << -a << -1523 << 8976 + a;

    OStream s2 = OutputFileStream("test10_output2.txt");
    s2 << a << abc;

    s << abc;
    
    s = OutputFileStream("test10_output.txt");
    s << t << false << true;
    s << abc << e << (2 == 2);
    s << 0 << -a << -1523 << 8976 + a;

    return 0;
}
