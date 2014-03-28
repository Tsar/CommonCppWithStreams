#include <iostream>

int f(int x, int y) {
    return x + y;
}

int main() {
    int a = 0, b = 0;
    a += b += 2;
    std::cout << a << " " << b << std::endl;

    a = 0; b = 0;
    a += (b += 2);
    std::cout << a << " " << b << std::endl;

    a = 0; b = 0;
    (a += b) += 2;
    std::cout << a << " " << b << std::endl;

    std::cout << f((2, 3), 5) << std::endl;

    a = (2, 3);
    std::cout << a << std::endl;

    a = 2, 3;
    5;
    std::cout << a << std::endl;

    return 0;
}
