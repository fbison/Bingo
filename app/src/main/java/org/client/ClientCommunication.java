package org.client;

import org.shared.CommunicationBase;
import org.shared.logs.LogMaker;

import java.io.IOException;
import java.net.Socket;

public class ClientCommunication extends CommunicationBase {

    public ClientCommunication(Socket socket) {
        super(socket);
    }

    public void connectToServer(String host, int port) {
        connect(host, port);
    }

    public void sendToServer(Object data) {
        send(data);
    }

    public Object receiveFromServer() {
        return receive();
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }
}
