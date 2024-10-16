package org.server;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.*;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerServer {
    private UUID id;
    private String name;
    private String password;
    private List<BingoCard> cards;
    private RoomServer currentRoom;
    private final ServerCommunication communication;

    public PlayerServer(Socket socket) {
        communication = new ServerCommunication(this, socket);
        id = UUID.randomUUID();  // Gera um UUID único para o jogador
    }

    //trocar aqui para MessageProtocol
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
    public void handleMessage(MessageProtocol message) {
        // Direciona o tratamento de acordo com o tipo de mensagem
        switch (message.type()) {
            case CADASTRO_USUARIO:
                handleCadastroUsuario((AuthenticationMessage)message.data());
                break;
            case LOG_IN:
                handleLogIn((AuthenticationMessage)message.data());
                break;
            case ENTRAR_SALA:
                handleEntrarSala((RoomMessage)message.data());
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
            case PONG:
                getCommunication().handlePong();
                break;
            default:
                LogMaker.info("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    // Função que processa o cadastro de um novo usuário
    private void handleCadastroUsuario(AuthenticationMessage data) {

        // Verifica se o usuário já existe
        for (PlayerServer player : Server.registeredPlayers) {
            if (player.name != null && player.name.equals(data.username())) {
                LogMaker.info("Usuário já registrado: " + data.username());
                send(new MessageProtocol(MessageType.ERRO,"Usuário já registrado"));
                return;
            }
        }
        this.name = data.username();
        this.password = data.password();
        Server.registeredPlayers.add(this);
        LogMaker.info("Cadastro bem-sucedido para o usuário: " + data.username());
        send("Cadastro bem-sucedido");
    }

    // Função que processa o login do usuário
    private void handleLogIn(AuthenticationMessage data) {
        LogMaker.info("Tratando login de usuário: " + data);

        for (PlayerServer player : Server.registeredPlayers) {
            if (player.name.equals(data.username()) && player.password.equals(password)) {
                // é preciso fazer isso por que essa instância do player representa o cliente,
                // então após  ele logar o player dessa comunicação mudou
                this.id = player.id;
                this.name = data.username();
                this.password = data.password();

                LogMaker.info("Login bem-sucedido para o usuário: " + data.username());
                send(new MessageProtocol(MessageType.SUCESSO_LOG_IN,new LogInReturnMessage(id, data.username())));

                // Associe o jogador à lista de jogadores online
                Server.getInstance().addOnlinePlayer(this);
                return;
            }
        }
        LogMaker.info("Falha no login para o usuário: " + data.username());
        send(new MessageProtocol(MessageType.ERRO,"Falha no login"));
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
                send(new MessageProtocol(MessageType.ERRO,"Você não está em uma sala."));
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
    private void handleEntrarSala(RoomMessage data) {
        LogMaker.info("Tratando entrada na sala: " + data);

        try {
            int roomId = data.roomId();

            // Verificar se a sala existe
            for (RoomServer room : Server.rooms) {
                if (room.getId() == roomId) {
                    currentRoom = room;
                    room.addPlayer(this);  // Adiciona o jogador à sala
                    LogMaker.info("Jogador " + name + " entrou na sala " + roomId);
                    send("Entrada na sala bem-sucedida");
                    return;
                }
            }

            LogMaker.info("Falha ao entrar na sala: Sala não encontrada");
            send(new MessageProtocol(MessageType.ERRO,"Sala não encontrada"));

        } catch (Exception e) {
            LogMaker.error("Erro ao processar a entrada na sala: " + e.getMessage());
        }
    }
    // Processa a solicitação de Bingo do cliente
    private void handleBingo(BingoMessage data) {
        if (currentRoom == null ||  currentRoom.getId()!=data.roomId()) {
            LogMaker.warn("O usuário não está na sala citada na mensagem");
            send(new MessageProtocol(MessageType.ERRO,
            "Erro: Você não está na sala mencionada."));
            return;
        }

        if (!getId().equals(data.playerId())) {
            LogMaker.warn("Mensagem enviada com assinatura de usuário errada");
            send(new MessageProtocol(MessageType.ERRO,"Erro: ID de jogador incorreto."));
            return;
        }

        LogMaker.info("Bingo recebido para verificação.");
        currentRoom.receiveBingos(data);
    }
}
