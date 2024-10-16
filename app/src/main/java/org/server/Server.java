package org.server;

import org.shared.JsonParser;
import org.shared.logs.LogMaker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {
    private static Server instance;
    private static final int PORT = 12345;
    public static List<PlayerServer> registeredPlayers = new ArrayList<>();
    private final List<PlayerServer> onlinePlayers = new ArrayList<>(); // Lista de jogadores online
    public static List<RoomServer> rooms = new ArrayList<>();

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
            createRooms(3);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LogMaker.info("Cliente conectado: " + clientSocket.getInetAddress());

                PlayerServer player = new PlayerServer(clientSocket);
                onlinePlayers.add(player);  // Adiciona o jogador à lista de onlinePlayers

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
        if (!onlinePlayers.contains(player)) {
            onlinePlayers.add(player);
            LogMaker.info("Jogador " + player.getId() + " adicionado à lista de jogadores online.");
        } else {
            LogMaker.warn("Jogador " + player.getId() + " já está na lista de jogadores online.");
        }
    }

    // Método para criar n salas no servidor
    public void createRooms(int numberOfRooms) {
        for (int i = 1; i <= numberOfRooms; i++) {
            RoomServer room = new RoomServer("Sala " + i);
            rooms.add(room);
            LogMaker.info("Sala criada: " + room.getId() + " - " + room.getName());
        }
    }

    // Envia a lista de salas disponíveis para um jogador específico
    public void sendRoomsToPlayer(PlayerServer player) {
        List<Map<String, Object>> roomsData = new ArrayList<>();
        for (RoomServer room : rooms) {
            Map<String, Object> roomInfo = Map.of(
                    "id", room.getId(),
                    "name", room.getName()
            );
            roomsData.add(roomInfo);
        }

        String roomsJson = JsonParser.toJson(roomsData);
        player.send(roomsJson);
        LogMaker.info("Lista de salas enviada para o jogador: " + player.getId());
    }
}

