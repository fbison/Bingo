package client;
import  common.CommunicationBase;

public class ClientCommunication extends CommunicationBase {
    public void connectToServer(String host, int port) {
        // Conectar ao servidor
        connect(host, port);
    }
}
