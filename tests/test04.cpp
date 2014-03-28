#include <iostream>

int main() {
    int i = 0;
    int x = ++i + ++i;
    i = 0;
    int y = i++ + i++;
    std::cout << x << " " << y << std::endl;
    return 0;
}
