//
// Created by shond on 1/6/19.
//

#ifndef BOOST_ECHO_CLIENT_OUTPUT_H
#define BOOST_ECHO_CLIENT_OUTPUT_H

#include "connectionHandler.h"

class output {
private:
    ConnectionHandler &connection;
    bool &shouldTerminate;

public:
    void main();
    output(ConnectionHandler &connection, bool &shouldTerminate);
};


#endif //BOOST_ECHO_CLIENT_OUTPUT_H
