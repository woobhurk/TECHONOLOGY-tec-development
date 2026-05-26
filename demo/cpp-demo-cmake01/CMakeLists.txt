# Using the fllowing command to generate build files:
# cmake . -DCMAKE_C_COMPILER=gcc -DCMAKE_CXX_COMPILER=g++ -G "MinGW Makefiles"

cmake_minimum_required(VERSION 3.20)

project("CMakeTest")

aux_source_directory(${PROJECT_SOURCE_DIR} SRC_FILE_LIST)
include_directories(${PROJECT_BINARY_DIR})

add_executable(${PROJECT_NAME} ${SRC_FILE_LIST})
