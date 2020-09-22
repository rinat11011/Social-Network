package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.NonBlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class  ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer,ConnectionHandler<T>> connections = new ConcurrentHashMap<>();
    private ConnectionHandler<T> client;

    public synchronized boolean send(int connectionId, T msg){
        client = connections.get(connectionId);
        if(client!=null) { //if the client does'nt exist return false,otherwise send him the message
            client.send(msg);
            return true;
        }
        else
            return false;

    }

    public synchronized void broadcast(T msg){
        connections.forEach((key,val) -> val.send(msg)); //send msg to each client in the connection list

    }

    public synchronized void add(int conId,ConnectionHandler<T> handler){
        if(this.connections.get(conId)==null)
            connections.put(conId,handler);
    }

    public synchronized void disconnect(int connectionId){
        connections.remove(connectionId);
    }
}
