cmake_minimum_required(VERSION 3.10.2)

project("devices")

file(GLOB soure
        ${CMAKE_SOURCE_DIR}/*.cpp
        ${CMAKE_SOURCE_DIR}/utils/*.cpp
        )

add_library(
        nativeD-lib

        SHARED

        ${soure})

find_library(
        log-lib

        log)

#引入头文件
include_directories(${CMAKE_SOURCE_DIR}/hpsocket)
#导入.so库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries(
        nativeD-lib
        ${log-lib}

        #忽略.so 动态库的先后顺序
        -Wl,--start-group
        networkY
        hpsocket
        hpsocket4c

        ffmpeg
        iMVR
        tcpsender
        -Wl,--end-group
        android)