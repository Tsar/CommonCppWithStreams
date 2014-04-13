int a = 3 + 5 + 7, b;
int c = a += b += 7;

int b1 = 2 + 2 * 2, b2 = (2 + 2) * 2;

void f(int x, int y, bool z, bool q = true, int zz = 7 * 6 * 9 << 4) { // some comment
    return;
}

/*

Small story
небольшой блок текста

*/

int main() {
    while (c == 2) {
        print(45, 54, false, 12 * 12, c);
    }
    
    do {
        print(25);  // aba caba aba
    } while (c == 2);
    
    //for (i = 0; i < 15; ++i) {}
    
    for (; 7 == 9; i++);
    
    int d;
    if (a == 3)
        return 2;
    else {
        return 9;
    }
    
    IStream x = InputStream();
    OStream y;
    y = OutputFileStream("out.txt");
    
    x >> a >> b >> c >> d;
    y << a + b << b << c << (b << c);
}

int abc = ++ ++ a;
