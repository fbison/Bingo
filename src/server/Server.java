package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private final List<PlayerServer> registeredPlayers = new ArrayList<>();
    private final List<PlayerServer> onlinePlayers = new ArrayList<>();
    private final List<RoomServer> rooms = new ArrayList<>();

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor rodando na porta " + PORT);

            while (true) {
                // Aguarda a conexão de um cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Cria uma nova comunicação com o cliente em uma thread separada
                PlayerServer player = new PlayerServer(clientSocket);
                onlinePlayers.add(player);

                // Inicia uma nova thread para gerenciar a comunicação com o cliente
                new Thread(player.getCommunication()).start();
            }

        }
        catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleClientConnection(Socket client) {
        // Lidar com a conexão do cliente
    }

    // Broadcast para players (duplicado no Server e Room Server) talvez colocar em alguma biblioteca Util que receba uma lista de players com as mensagens
    public void broadcast(Object object) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (PlayerServer player : onlinePlayers) {
                //no protocolo é preciso alterar as partes relacionadas ao usuário, fazer isso quando finalizado
                executor.submit(() -> {
                    player.send(object);
                });
            }
        }
    }
    public void sendRooms() {
        broadcast(rooms); // tratar o modelo de envio ainda
    }

    public void updateOnlinePlayers() {
        // Atualizar lista de jogadores online com keep-alive, verificando os sockets ainda conectados, ou deslogados
        // essa desconexão é feita no server, talvez não seja preciso verificar o keepAlive, mas verificar se o thread foi desconectado
        // talvez o ServerCOmunication pode enviar uma exceção com qual foi desconectado e o próprio starServer desconecta
    }
}
