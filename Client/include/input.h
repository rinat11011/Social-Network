//
// Created by shond on 1/6/19.
//

#ifndef BOOST_ECHO_CLIENT_INPUT_H
#define BOOST_ECHO_CLIENT_INPUT_H
#include "connectionHandler.h"

class input {
private:
    ConnectionHandler &connection;
    bool &shouldTerminate;

public:
    void main();
    input(ConnectionHandler &connection, bool &shouldTerminate);
};


#endif //BOOST_ECHO_CLIENT_INPUT_H
