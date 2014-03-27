int a;
int b = 3, c = 4;
char d = 5;
long e = 11, f;
long g, h = 15;

long f1(int a = 10, char b = 3) {
    int x = 20;
    return 50000;
}

void f2(char a, char b) {
}

void f3(char a, int b = f1(14) * 3) {
}

int main() {
    return 0;
}
