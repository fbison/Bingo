package server;

import java.net.Socket;
import java.util.List;

public class Server {
    private SocketService socketService;
    private List<PlayerServer> registeredPlayers;
    private List<PlayerServer> onlinePlayers;
    private List<RoomServer> rooms;
    private KeepAliveService keepAliveService;

    public void startServer() {
        // Iniciar o servidor
    }

    public void handleClientConnection(Socket client) {
        // Lidar com a conexão do cliente
    }

    public void sendRooms() {
        this.rooms;
    }

    public void updateOnlinePlayers() {
        // Atualizar lista de jogadores online com keep-alive
    }
}
