package org.client;

import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.MessageProtocol;
import org.shared.messages.MessageType;

import java.net.Socket;
import java.util.Map;

public class BingoClient {
    private ClientCommunication clientCommunication;
    private RoomClient roomOpen;
    private PlayerClient playerLoggedIn;

    // Conecta ao servidor usando TCP e inicializa a comunicação
    public void connectToServer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            clientCommunication = new ClientCommunication(socket);
            LogMaker.info("Conectado ao servidor: " + host + ":" + port);

            // Inicia uma thread para escutar mensagens do servidor
            new Thread(this::listenToServerMessages).start();
        } catch (Exception e) {
            LogMaker.error("Erro ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Login de usuário
    public void login(String username, String password) {
        MessageProtocol loginMessage = new MessageProtocol(MessageType.LOG_IN,
                Map.of("username", username, "password", password));
        clientCommunication.sendToServer(JsonParser.toJson(loginMessage));
    }

    // Registro de novo usuário
    public void register(String username, String password) {
        MessageProtocol registerMessage = new MessageProtocol(MessageType.CADASTRO_USUARIO,
                Map.of("username", username, "password", password));
        clientCommunication.sendToServer(JsonParser.toJson(registerMessage));
    }

    // Solicita a lista de salas ao servidor
    public void requestRooms() {
        MessageProtocol roomsRequestMessage = new MessageProtocol(MessageType.SALAS_DISPONIVEIS, null);
        clientCommunication.sendToServer(JsonParser.toJson(roomsRequestMessage));
        LogMaker.info("Solicitação de lista de salas enviada.");
    }

    // Ouvindo as mensagens recebidas do servidor em uma thread separada
    private void listenToServerMessages() {
        while (true) {
            Object message = clientCommunication.receiveFromServer();
            if (message == null) {
                LogMaker.warn("Conexão perdida com o servidor.");
                break;
            }

            // Verifica qual tipo de mensagem está recebendo e direciona ao objeto correto
            if (playerLoggedIn != null) {
                playerLoggedIn.handleMessage(message.toString());
            } else if (roomOpen != null) {
                roomOpen.handleMessage(message.toString());
            }
        }
    }

    // Desconecta o cliente
    public void disconnect() {
        clientCommunication.disconnect();
    }

    // Entrar em uma sala específica pelo ID
    public void enterRoom(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            requestRooms();  // Solicita as salas se não tiver um ID ainda
            return;
        }

        String enterRoomMessage = String.format("{\"type\": \"ENTRAR_SALA\", \"data\": {\"roomId\": \"%s\"}}", roomId);
        clientCommunication.sendToServer(enterRoomMessage);
        LogMaker.info("Solicitação para entrar na sala enviada.");
    }

    // Envia a declaração de Bingo para o servidor
    public void sendBingo() {
        if (playerLoggedIn != null) {
            String bingoMessage = String.format("{\"type\": \"BINGO\", \"data\": {\"playerId\": \"%s\"}}", playerLoggedIn.getId());
            clientCommunication.sendToServer(bingoMessage);
            LogMaker.info("Solicitação de Bingo enviada.");
        } else {
            LogMaker.warn("Jogador não está logado.");
        }
    }
}
