package server;

import common.CommunicationBase;

import java.net.Socket;

public class ServerCommunication extends CommunicationBase implements Runnable{

    private static final long KEEP_ALIVE_INTERVAL = 5000; // Intervalo para envio do keep-alive (5 segundos)
    private static final long TIMEOUT = 15000; // Timeout de 15 segundos se não houver resposta

    private long lastPongReceivedTime; // Marca o tempo da última resposta "pong"
    private boolean keepAliveEnabled = true;
    private PlayerServer player;

    public ServerCommunication(PlayerServer player, Socket clientSocket){
        super(clientSocket);
        this.player = player;
        this.lastPongReceivedTime = System.currentTimeMillis();
    }
    // O que será executado na thread
    @Override
    public void run() {
        handleClient();
    }

    // Método que gerencia a comunicação com o cliente (recebimento de dados)
    public void handleClient() {
        try {
            startKeepAliveThread();
            // Receber e tratar mensagens do cliente
            while (true) {
                Object receivedData = receive();
                if (receivedData == null) {
                    disconnect();
                    break;
                }
                processReceivedData(receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    // Inicia a thread de Keep-Alive
    private void startKeepAliveThread() {
        Thread keepAliveThread = new Thread(() -> {
            while (keepAliveEnabled) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPongReceivedTime > TIMEOUT) {
                        System.out.println("Timeout: cliente não respondeu ao keep-alive.");
                        disconnect();
                        break;
                    }
                    sendKeepAlive();
                    Thread.sleep(KEEP_ALIVE_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        keepAliveThread.start();
    }


    // Processa os dados recebidos
    private void processReceivedData(Object receivedData) {
        if (!(receivedData instanceof String message)) {
            System.out.println("Tipo de mensagem desconhecido.");
            return;
        }
        if ("pong".equals(message)) {
            lastPongReceivedTime = System.currentTimeMillis();
            System.out.println("Pong recebido.");
        } else {
            player.handleMessage(message);
        }
    }

    private void sendKeepAlive() {
        try {
            send("ping");
            System.out.println("Ping enviado.");
        } catch (Exception e) {
            System.err.println("Erro ao enviar o ping: " + e.getMessage());
        }
    }

    public void disconnect() {
        System.out.println("Cliente desconectado.");
        keepAliveEnabled = false;
        super.disconnect();
    }

}
