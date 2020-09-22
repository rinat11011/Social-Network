package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.Users;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.MessageEncoderDecoderImpl;

public class ReactorMain {
    public static void main(String[]args) {
        Users users = new Users();
        int port = Integer.parseInt(args[0]);
        int numOfThreads = Integer.parseInt(args[1]);
        Server.reactor(numOfThreads,port,()->new BidiMessagingProtocolImpl<>(users)
                                   ,()->new MessageEncoderDecoderImpl<>()).serve();
    }
}
