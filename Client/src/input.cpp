//
// Created by shond on 1/6/19.
//

#include "input.h"
#include "connectionHandler.h"
using namespace std;

input::input(ConnectionHandler &connection, bool &shouldTerminate):connection(connection), shouldTerminate(shouldTerminate) {}

void input::main(){
    while (!shouldTerminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);
        if (!connection.sendLine(line)) {
            cout << "Disconnected. Exiting..." << endl;
            break;
        }
    }
}