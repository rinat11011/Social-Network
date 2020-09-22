#include <stdlib.h>
#include <connectionHandler.h>
#include "output.h"
#include "input.h"
#include <boost/thread.hpp>

using namespace std;

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        return -1;
    }
    string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        return 1;
    }

    bool shouldTerminate = false;

    output output(connectionHandler, shouldTerminate);
    input input(connectionHandler,shouldTerminate);
    boost::thread t1(&output::main, &output);
    boost::thread t2(&input::main,&input);

    t1.join();
    t2.join();
    return 0;
}
