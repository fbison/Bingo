package server;

import common.BingoCard;

import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class PlayerServer {
    private UUID id;
    private String name;
    private String password;
    private List<BingoCard> cards;
    private final ServerCommunication comunication;

    public PlayerServer (Socket socket){
        comunication = new ServerCommunication(socket);
        id = UUID.randomUUID();
    }

    public void send(Object object){
        comunication.send(object);
    }

    public ServerCommunication getCommunication(){
        return comunication;
    }


    public UUID getId() {
        return id;
    }
}
