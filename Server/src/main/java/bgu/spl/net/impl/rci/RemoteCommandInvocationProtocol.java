package bgu.spl.net.impl.rci;


import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.Serializable;

public class RemoteCommandInvocationProtocol<T> implements BidiMessagingProtocol<Serializable> {

    private T arg;
    private int connId;
    private Connections<Serializable> connections;

    public RemoteCommandInvocationProtocol(T arg) {
        this.arg = arg;
    }

    public void start(int connectionId, Connections<Serializable> connections) {
        this.connections = connections;
        this.connId = connectionId;
    }

    public void process(Serializable msg) {
        connections.send(connId,((Command) msg).execute(arg));
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

}
