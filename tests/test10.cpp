bool abc = false;
bool e = true;

int main()
{
    bool t = true;
    OStream s = OutputStream();
    int a = 15;
    s << t << false << true;
    s << abc << e << (2 == 2);
    s << 0 << -a << -1523 << 8976 + a;
    return a;
}
