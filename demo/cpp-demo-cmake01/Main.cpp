#include <iostream>
#include "Main.h"

using namespace com_tyfanchz_cpp;

void App::showToast(char* str) {
    std::cout << "Your name is: " << str << std::endl;
    this->privShowHello();
}

void App::privShowHello() {
    std::cout << "This is a private method." << std::endl;
}

int main(void) {
    std::cout << "Hello cmake!" << std::endl;
    App app;
    app.showToast("哈哈！！");
    return 0;
}
