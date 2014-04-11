#include <iostream>

int a = 1;

void f(int a) {
    std::cout << a << std::endl;
    {
        int a = 5;
        std::cout << a << std::endl;
    }
    std::cout << a << std::endl;
}

int main() {
    int q1 = false;
    int q2 = true;
    bool q3 = 15;
    bool q4 = 0;
    if ((2 != 2) || (false == -10)) {
        return false + true * false;
    }
    
    std::cout << a << std::endl;
    
    int a = 2;
    std::cout << a << std::endl;
    
    {
        int a = a * 2;
        std::cout << a << std::endl;
    }

    std::cout << a << std::endl;

    f(4);
    
    return 0;
}
