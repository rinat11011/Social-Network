package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T>{

    private final BidiMessagingProtocolImpl<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl connections;
    private int id;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader,
                                     BidiMessagingProtocolImpl<T> protocol, ConnectionsImpl connections,int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.id = connectionId;
    }

    @Override
    public void run() {
       connections.add(id,this);
       protocol.start(id,connections);
        try {
            try (Socket sock = this.sock) { //just for automatic closing
                int read;

                in = new BufferedInputStream(sock.getInputStream());
                out = new BufferedOutputStream(sock.getOutputStream());

                while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                    T nextMessage = encdec.decodeNextByte((byte) read);
                    if (nextMessage != null) {
                            protocol.process(nextMessage);
                        }
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public synchronized void send(T msg){
        if((connected) && (msg!=null)){
            try {
                out = new BufferedOutputStream(sock.getOutputStream());
                out.write(encdec.encode(msg));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
