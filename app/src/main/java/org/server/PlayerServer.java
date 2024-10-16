package org.server;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.BingoMessage;
import org.shared.messages.MessageProtocol;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerServer {
    private final UUID id;
    private String name;
    private String password;
    private List<BingoCard> cards;
    private RoomServer currentRoom;
    private final ServerCommunication communication;

    public PlayerServer(Socket socket) {
        communication = new ServerCommunication(this, socket);
        id = UUID.randomUUID();  // Gera um UUID único para o jogador
    }

    public void send(Object object) {
        communication.send(object);
    }

    public ServerCommunication getCommunication() {
        return communication;
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    // Função que processa as mensagens recebidas do cliente
    public void handleMessage(String originalMessage) {
        MessageProtocol message;
        try {
            message = JsonParser.parseJson(originalMessage, MessageProtocol.class);
        } catch (Exception e) {
            LogMaker.info("Mensagem em formato errado");
            return;
        }

        // Direciona o tratamento de acordo com o tipo de mensagem
        switch (message.type()) {
            case CADASTRO_USUARIO:
                handleCadastroUsuario(message.data());
                break;
            case LOG_IN:
                handleLogIn(message.data());
                break;
            case ENTRAR_SALA:
                handleEntrarSala(message.data());
                break;
            case ENVIAR_CARTELA:
                handleEnviarCartela(message.data());
                break;
            case SALAS_DISPONIVEIS:
                handleRequestRooms();
                break;
            case BINGO:
                handleBingo((BingoMessage) message.data());
                break;
            default:
                LogMaker.info("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    // Função que processa o cadastro de um novo usuário
    private void handleCadastroUsuario(Object data) {
        if (!(data instanceof Map)) {
            LogMaker.error("Dados de cadastro inválidos.");
            send("Erro ao cadastrar: Dados inválidos.");
            return;
        }

        Map<String, String> cadastroData = (Map<String, String>) data;
        String username = cadastroData.get("username");
        String password = cadastroData.get("password");

        // Verifica se o usuário já existe
        for (PlayerServer player : Server.registeredPlayers) {
            if (player.name != null && player.name.equals(username)) {
                LogMaker.info("Usuário já registrado: " + username);
                send("Usuário já registrado");
                return;
            }
        }

        this.name = username;
        this.password = password;
        Server.registeredPlayers.add(this);
        LogMaker.info("Cadastro bem-sucedido para o usuário: " + username);
        send("Cadastro bem-sucedido");
    }

    // Função que processa o login do usuário
    private void handleLogIn(Object data) {
        LogMaker.info("Tratando login de usuário: " + data);
        if (!(data instanceof Map)) {
            LogMaker.error("Dados de login inválidos.");
            send("Dados de login inválidos.");
            return;
        }

        Map<String, String> loginData = (Map<String, String>) data;
        String username = loginData.get("username");
        String password = loginData.get("password");

        for (PlayerServer player : Server.registeredPlayers) {
            if (player.name.equals(username) && player.password.equals(password)) {
                this.name = username;
                this.password = password;
                LogMaker.info("Login bem-sucedido para o usuário: " + username);
                send("Login bem-sucedido");

                // Associe o jogador à lista de jogadores online
                Server.getInstance().addOnlinePlayer(this);
                return;
            }
        }
        LogMaker.info("Falha no login para o usuário: " + username);
        send("Falha no login");
    }

    // Função que processa a solicitação de lista de salas
    private void handleRequestRooms() {
        LogMaker.info("Solicitação de lista de salas recebida.");
        Server.getInstance().sendRoomsToPlayer(this);
    }

    // Função que processa o envio de cartelas
    private void handleEnviarCartela(Object data) {
        LogMaker.info("Tratando envio de cartela: " + data);

        try {
            BingoCard cartela = JsonParser.parseJson(data.toString(), BingoCard.class);

            if (currentRoom == null) {
                LogMaker.info("Falha ao enviar cartela: Jogador não está em uma sala");
                send("Você não está em uma sala.");
                return;
            }

            // Adiciona a cartela à lista de cartelas do jogador
            this.cards.add(cartela);
            LogMaker.info("Cartela enviada com sucesso para o jogador " + name);
            send("Cartela enviada com sucesso");

        } catch (Exception e) {
            LogMaker.error("Erro ao processar o envio da cartela: " + e.getMessage());
        }
    }

    // Função que trata a entrada do usuário na sala
    private void handleEntrarSala(Object data) {
        LogMaker.info("Tratando entrada na sala: " + data);

        try {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String roomId = (String) dataMap.get("roomId");

            // Verificar se a sala existe
            for (RoomServer room : Server.rooms) {
                if (String.valueOf(room.getId()).equals(roomId)) {
                    currentRoom = room;
                    room.addPlayer(this);  // Adiciona o jogador à sala
                    LogMaker.info("Jogador " + name + " entrou na sala " + roomId);
                    send("Entrada na sala bem-sucedida");
                    return;
                }
            }

            LogMaker.info("Falha ao entrar na sala: Sala não encontrada");
            send("Sala não encontrada");

        } catch (Exception e) {
            LogMaker.error("Erro ao processar a entrada na sala: " + e.getMessage());
        }
    }
    // Processa a solicitação de Bingo do cliente
    private void handleBingo(BingoMessage data) {
        if (currentRoom == null ||  currentRoom.getId()!=data.roomId()) {
            LogMaker.warn("O usuário não está na sala citada na mensagem");
            send("Erro: Você não está na sala mencionada.");
            return;
        }

        if (!getId().equals(data.playerId())) {
            LogMaker.warn("Mensagem enviada com assinatura de usuário errada");
            send("Erro: ID de jogador incorreto.");
            return;
        }

        LogMaker.info("Bingo recebido para verificação.");
        currentRoom.receiveBingos(data);
    }
}
