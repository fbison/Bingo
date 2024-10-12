package server;

import common.BingoCard;
import common.JsonParser;
import common.Messages.BingoMessage;
import common.Messages.MessageProtocol;

import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class PlayerServer {
    private final UUID id;
    private String name;
    private String password;
    private List<BingoCard> cards;
    private RoomServer currentRoom;
    private final ServerCommunication comunication;

    public PlayerServer (Socket socket){
        comunication = new ServerCommunication(this, socket);
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

    //Faz o parser da mensagem pra java
    public void handleMessage(String originalMessage) {
        MessageProtocol message;
        try {
             message = JsonParser.parseJson(originalMessage, MessageProtocol.class);
        }catch (Exception e){
            System.out.println("Mensagem em formato errado");
            return;
        }
        switch (message.type()) {
            case CADASTRO_USUARIO:
                handleCadastroUsuario(message.data());
                break;
            case ENTRAR_SALA:
                handleEntrarSala(message.data());
                break;
            case ENVIAR_CARTELA:
                handleEnviarCartela(message.data());
                break;
            case BINGO:
                handleBingo((BingoMessage)message.data());
                break;
            default:
                System.out.println("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    //Trata o envio da mensagem do bingo para a sala
    private void handleBingo(BingoMessage data) {
        // Lógica para o bingo
        if(currentRoom == null || currentRoom.getId().equals(data.roomId())) {
            System.out.println("O usuário não está na sala citada na mensagem");
            return;
        };
        if(getId().equals(data.playerId())) {
            System.out.println("Mensagem enviada com assinatura de usuário errada");
            return;
        };
        currentRoom.receiveBingos(data);
    }

    // Lógica para tratar o LogIn
    private void handleLogIn(Object data) {
        System.out.println("Tratando cadastro de usuário: " + data);
    }
    // Lógica para tratar o cadastro de usuário

    private void handleCadastroUsuario(Object data) {
        System.out.println("Tratando cadastro de usuário: " + data);
    }

    // Lógica para entrar na sala
    private void handleEntrarSala(Object data) {
        System.out.println("Tratando entrada na sala: " + data);
    }

    // Lógica para enviar cartela
    private void handleEnviarCartela(Object data) {
        System.out.println("Tratando envio de cartela: " + data);
    }


}
