//
// Created by shond on 1/6/19.
//

#include <string>
#include <iostream>
#include "output.h"
#include "connectionHandler.h"
using namespace std;

output::output(ConnectionHandler &connection, bool &shouldTerminate) : connection(connection), shouldTerminate(shouldTerminate) {
}

void output::main() {
    string output;
    while ( !shouldTerminate ) {
        output.resize(0);
        if (!this->connection.getLine(output)) {
            break;
        }
        output.resize(output.length());
        if(output.length()>0)
            cout << output << endl;
        if(output == "ACK LOGOUT") {
            shouldTerminate = true;
        }
    }
    cout << "DONE" << endl;

}