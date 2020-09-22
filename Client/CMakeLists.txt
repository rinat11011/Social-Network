cmake_minimum_required(VERSION 3.12)
project(Boost_Echo_Client)

set(CMAKE_CXX_STANDARD 14)

find_package(Boost COMPONENTS system filesystem thread REQUIRED)

IF (Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIR})
endif()

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread -lboost_thread")
include_directories(${Boost_INCLUDE_DIRS})



include_directories(include)
add_executable(
        Boost_Echo_Client
        include/connectionHandler.h
        src/connectionHandler.cpp
        src/echoClient.cpp
        src/input.cpp
        include/input.h
        include/output.h
        src/output.cpp
        makefile)


target_link_libraries( Boost_Echo_Client LINK_PUBLIC ${Boost_LIBRARIES} )