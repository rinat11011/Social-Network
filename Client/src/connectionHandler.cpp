#include <connectionHandler.h>
#include <boost/lexical_cast.hpp>

class error_code;

using boost::asio::ip::tcp;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

// Number to string conversion:


ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;

}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;


}

bool ConnectionHandler::getLine(std::string& line) {//decode
    char opCodeBytes[2];
    getBytes(opCodeBytes,2);
    short opcode = bytesToShort(opCodeBytes);

    switch(opcode){
        case 0: {
            return true;
        }
        case 9 :{ //Notification
            line.append("NOTIFICATION ");
            char typeBytes[1];
            getBytes(typeBytes,1);
            if(typeBytes[0]=='0') //pm
                line.append("PM ");
            else
                line.append("Public ");
            getFrameAscii(line,'\0');
            line = line.substr(0,line.length()-1);
            line.append(" ");

            getFrameAscii(line,'\0');
            line = line.substr(0,line.length()-1);
            break;
        }

        case 10 :{ //ACK
            line.append("ACK ");
            char msgOpCodeBytes[2];
            getBytes(msgOpCodeBytes,2);
            short msgOpcode = bytesToShort(msgOpCodeBytes);
            line.append(boost::lexical_cast<string>(msgOpcode));

            switch (msgOpcode){
                case 4:{
                    line.append(" ");
                    char numOfUsersBytes[2];
                    getBytes(numOfUsersBytes,2);//gets the num of users
                    short numOfUsers = bytesToShort(numOfUsersBytes);
                    line.append(boost::lexical_cast<string>(numOfUsers));
                    line.append(" ");
                    getFrameAscii(line,'\0');//gets the list of users and adds it to line
                    line = line.substr(0,line.length()-1);
                    break;

                }
                case 7:{
                    line.append(" ");
                    char numOfUsersBytes[2];
                    getBytes(numOfUsersBytes,2);//gets the num of users
                    short numOfUsers = bytesToShort(numOfUsersBytes);
                    line.append(boost::lexical_cast<string>(numOfUsers));
                    line.append(" ");
                    getFrameAscii(line,'\0');//gets the list of users and adds it to line
                    line = line.substr(0,line.length()-1);
                    break;
                }
                case 8:{
                    line.append(" ");
                    char numOfPostsBytes[2];
                    getBytes(numOfPostsBytes,2);//gets the num of posts
                    short numOfPosts = bytesToShort(numOfPostsBytes);
                    line.append(boost::lexical_cast<string>(numOfPosts));
                    line.append(" ");
                    char numOfFollowersBytes[2];
                    getBytes(numOfFollowersBytes,2);//gets the num of followers
                    short numOfFollowers = bytesToShort(numOfFollowersBytes);
                    line.append(boost::lexical_cast<string>(numOfFollowers));
                    line.append(" ");
                    char numOfFollowingBytes[2];
                    getBytes(numOfFollowingBytes,2);//gets the num of following
                    short numOfFollowing = bytesToShort(numOfFollowingBytes);
                    line.append(boost::lexical_cast<string>(numOfFollowing));
                    break;
                }
            }

            break;
        }

        case 11:{ //Error
            line.append("ERROR ");
            char msgOpCodeBytes[2];
            getBytes(msgOpCodeBytes,2);
            short msgOpcode = bytesToShort(msgOpCodeBytes);
            line.append(boost::lexical_cast<string>(msgOpcode));
            break;
        }
    }
    return true;
}

bool ConnectionHandler::sendLine(std::string& line) {//encode
    std::stringstream str(line);
    string command; //get the user's input
    getline(str,command,' ');
    char bytesToAdd[2];


    if(command == "REGISTER"){
        short opcode = 1;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);

        string user;
        getline(str,user,' ');
        sendFrameAscii(user,'\0'); //converts the string to bytes and sends them with sendBytes
        string password;
        getline(str,password,'\n');
        sendFrameAscii(password,'\0');
        return true;

    }

    else if(command == "LOGIN"){
        short opcode = 2;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);

        string user;
        getline(str,user,' ');
        sendFrameAscii(user,'\0'); //converts the string to bytes and sends them with sendBytes
        string password;
        getline(str,password,'\n');
        sendFrameAscii(password,'\0');
        return true;

    }
    else if(command == "LOGOUT"){
        short opcode = 3;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);
        return true;

    }

    else if(command == "FOLLOW"){
        short opcode = 4;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);

        string followUnfollow;
        getline(str,followUnfollow,' ');
        if(followUnfollow=="0"){
            sendFrameAscii("",'\0');
        }
        else sendFrameAscii("",'1');

        string numOfUsers;
        getline(str,numOfUsers,' ');
        short num = boost::lexical_cast<short>(numOfUsers);
        char bytesNum[2];
        shortToBytes(num,bytesNum);
        sendBytes(bytesNum,2);
        string user;
        getline(str,user,'\n');
        replace(user.begin(),user.end(),' ','\0');
        sendFrameAscii(user,'\0');
        return true;

    }

    else if(command == "POST"){
        short opcode = 5;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);

        string content;
        getline(str,content,'\n');
        sendFrameAscii(content,'\0');
        return true;

    }
    else if(command == "PM"){
        short opcode = 6;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);
        string user;
        string content;
        getline(str,user,' ');
        getline(str,content,'\n');
        sendFrameAscii(user,'\0');
        sendFrameAscii(content,'\0');
        return true;


    }
    else if(command == "USERLIST"){
        short opcode = 7;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);
        return true;

    }
    else if(command == "STAT"){
        short opcode = 8;
        shortToBytes(opcode,bytesToAdd);
        sendBytes(bytesToAdd,2);
        string user;
        getline(str,user,' ');
        sendFrameAscii(user,'\0');
        return true;
    }

    return false;
}

bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do{
            getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    bool result=sendBytes(frame.c_str(),frame.length());
    if(!result) return false;
    return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);

}

short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}