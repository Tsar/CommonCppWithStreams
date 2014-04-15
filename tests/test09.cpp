int a;
int b;
int c;
int d;
int e;
int g;
int h;

int f(int x) {
    {
        {
            {
                x = a++;
                return x + 1;
            }
        }
    }
}

int main() {
    f(2);
    f(4);
    return a;
}
