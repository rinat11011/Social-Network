package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.User;
import bgu.spl.net.api.bidi.Users;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.MessageEncoderDecoderImpl;

public class TPCMain {
    public static void main(String[]args){
        int port = Integer.parseInt(args[0]);
        Users users = new Users();

        Server.threadPerClient(port,()-> new BidiMessagingProtocolImpl(users)
                                   , MessageEncoderDecoderImpl::new).serve();
    }
}
