package org.server;

import org.shared.logs.LogMaker;
import org.shared.messages.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static Server instance;
    private static final int PORT = 12345;
    private static final int QTD_THREADS_SALAS = 3;
    public static List<AuthenticationMessage> registeredPlayers = new ArrayList<>(); // Registros de usuários
    public static HashMap<String, Boolean> isOnline = new HashMap<>(); // Registros de usuários online
    public static final List<PlayerServer> onlinePlayers = new ArrayList<>(); // Lista de jogadores online
    public static List<RoomServer> rooms = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(QTD_THREADS_SALAS);

    // Singleton para garantir que o servidor tenha apenas uma instância
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public static void main(String[] args) {
        Server.getInstance().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LogMaker.info("Servidor rodando na porta " + PORT);

            // Criar salas no servidor (por exemplo, 3 salas iniciais)
            createRooms(QTD_THREADS_SALAS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LogMaker.info("Cliente conectado: " + clientSocket.getInetAddress());

                PlayerServer player = new PlayerServer(clientSocket);
                onlinePlayers.add(player);

                // Inicia uma nova thread para gerenciar a comunicação com o cliente
                new Thread(player.getCommunication()).start();
            }
        } catch (IOException e) {
            LogMaker.error("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para adicionar jogador à lista de onlinePlayers
    public void addOnlinePlayer(PlayerServer player) {
        if (!Server.isOnline.get(player.getName())) {
            onlinePlayers.add(player);
            LogMaker.info("Jogador " + player.getId() + " adicionado à lista de jogadores online.");
        } else {
            LogMaker.warn("Jogador " + player.getId() + " já está na lista de jogadores online.");
        }
        sendRoomsToPlayer(player);
    }

    // Método para criar n salas no servidor
    public void createRooms(int numberOfRooms) {
        for (int i = 1; i <= numberOfRooms; i++) {
            RoomServer room = new RoomServer("Sala " + i);
            rooms.add(room);
            LogMaker.info("Sala criada: " + room.getId() + " - " + room.getName());

            // Inicia a execução de cada sala em uma thread
            executor.submit(room::run);
        }
        broadCastRooms();
    }

    private MessageProtocol formatToSendRooms(){
        ArrayList<RoomMessage> list = new ArrayList<>();
        for (RoomServer room : rooms) {
            list.add(new RoomMessage(room.getId(), room.getName()));
        }
        RoomsMessage data= new RoomsMessage(list);
        return new MessageProtocol(MessageType.SALAS_DISPONIVEIS, data);
    }

    //envia a lista de salas disponíveis para todos os usuários online
    public void broadCastRooms(){
        ServerUtils.broadcast(onlinePlayers, formatToSendRooms());
    }

    // Envia a lista de salas disponíveis para um jogador específico
    public void sendRoomsToPlayer(PlayerServer player) {
        LogMaker.info("Lista de salas enviada para o jogador: " + player.getId());
        player.send(formatToSendRooms());
    }
}

