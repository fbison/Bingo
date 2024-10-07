package common;

import java.net.Socket;

public abstract class CommunicationBase {
    protected Socket socket;

    public void send(Object data) {
        // Enviar dados pelo socket
    }

    public Object receive() {
        // Receber dados pelo socket
        return null;
    }

    public void connect(String host, int port) {
        // Conectar a um host
    }
}
