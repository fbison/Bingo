package org.client;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.*;

import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.shared.messages.MessageType.SUCESSO_LOG_IN;

public class BingoClient {
    private ClientCommunication clientCommunication;
    private PlayerClient playerLoggedIn;
    private BingoInterface bingoInterface;

    public PlayerClient getPlayerLoggedIn() {
        return playerLoggedIn;
    }

    // Conecta ao servidor usando TCP e inicializa a comunicação
    public void connectToServer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            clientCommunication = new ClientCommunication(socket);
            LogMaker.info("Conectado ao servidor: " + host + ":" + port);
            // Inicia uma thread para escutar mensagens do servidor
            new Thread(this::listenToServerMessages).start();
            startBingoInterface();
        } catch (Exception e) {
            LogMaker.error("Erro ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startBingoInterface() {
        bingoInterface = new BingoInterface(this);
        Thread interfaceThread = new Thread(bingoInterface);
        interfaceThread.start();

        try {
            // Aguarda a thread da interface do Bingo terminar
            interfaceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // Login de usuário
    public void login(String username, String password) {
        MessageProtocol loginMessage = new MessageProtocol(MessageType.LOG_IN,
                new AuthenticationMessage(username, password));
        clientCommunication.sendToServer(loginMessage);
    }

    // Registro de novo usuário
    public void register(String username, String password) {
        MessageProtocol registerMessage = new MessageProtocol(MessageType.CADASTRO_USUARIO,
                new AuthenticationMessage(username, password));
        clientCommunication.sendToServer(registerMessage);
    }

    // Solicita a lista de salas ao servidor
    public void requestRooms() {
        MessageProtocol roomsRequestMessage = new MessageProtocol(MessageType.SALAS_DISPONIVEIS, null);
        clientCommunication.sendToServer(roomsRequestMessage);
        LogMaker.info("Solicitação de lista de salas enviada.");
    }


    // Ouvindo as mensagens recebidas do servidor em uma thread separada
    private void listenToServerMessages() {
        while (true) {
            Object message = clientCommunication.receiveFromServer();
            if (message == null) {
                break;
            }
            handleMessage(message);
        }
    }


    public void handleMessage(Object receivedData) {
        if (!(receivedData instanceof MessageProtocol message)) {
            LogMaker.info("Tipo de mensagem desconhecido.");
            return;
        }

        switch (message.type()) {
            case PING:
                handlePing();
                break;
            case SUCESSO_LOG_IN:
                handleLogIn((LogInReturnMessage) message.data());
                break;
        }
        if (playerLoggedIn != null && message.type() != SUCESSO_LOG_IN) {
            playerLoggedIn.handleMessage(message);
        }
    }

    private void handleLogIn(LogInReturnMessage message){
        this.playerLoggedIn = new PlayerClient(message.playerId(), message.name(), clientCommunication);
    }
    private void handlePing(){
        clientCommunication.send(new MessageProtocol(MessageType.PONG, null));
    }

    // Desconecta o cliente
    public void disconnect() {
        clientCommunication.disconnect();
    }

    // Entrar em uma sala específica pelo ID
    public void enterRoom(int roomId) {
        MessageProtocol enterRoomMessage = new MessageProtocol(MessageType.ENTRAR_SALA, new RoomMessage(roomId, ""));
        clientCommunication.sendToServer(enterRoomMessage);
        LogMaker.info("Solicitação para entrar na sala enviada.");
    }

    // Envia a declaração de Bingo para o servidor
    public void sendBingo() {
        if (playerLoggedIn != null) {
            MessageProtocol bingoMessage = new MessageProtocol(MessageType.BINGO, new BingoMessage(
                    playerLoggedIn.getId(),
                    playerLoggedIn.getCurrentRoom().getId(),
                    playerLoggedIn.getCard(),
                    OffsetDateTime.now()
            ));
            clientCommunication.sendToServer(bingoMessage);
            LogMaker.info("Solicitação de Bingo enviada.");
        } else {
            LogMaker.warn("Jogador não está logado.");
        }
    }
}
