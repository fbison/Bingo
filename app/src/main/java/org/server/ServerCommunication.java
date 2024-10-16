package org.server;

import org.shared.CommunicationBase;
import org.shared.logs.LogMaker;

import java.net.Socket;

public class ServerCommunication extends CommunicationBase implements Runnable {

    private static final long KEEP_ALIVE_INTERVAL = 200000; // Intervalo para envio do keep-alive (20 segundos)
    private static final long TIMEOUT = 150000; // Timeout de 30 segundos se não houver resposta

    private long lastPongReceivedTime; // Marca o tempo da última resposta "pong"
    private boolean keepAliveEnabled = true;
    private PlayerServer player;

    public ServerCommunication(PlayerServer player, Socket clientSocket) {
        super(clientSocket);
        this.player = player;
        this.lastPongReceivedTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        handleClient();
    }

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

    private void startKeepAliveThread() {
        Thread keepAliveThread = new Thread(() -> {
            while (keepAliveEnabled) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPongReceivedTime > TIMEOUT) {
                        LogMaker.info("Timeout: cliente não respondeu ao keep-alive.");
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

    private void processReceivedData(Object receivedData) {
        if (!(receivedData instanceof String message)) {
            LogMaker.info("Tipo de mensagem desconhecido.");
            return;
        }
        if ("pong".equals(message)) {
            lastPongReceivedTime = System.currentTimeMillis();
            LogMaker.info("Pong recebido.");
        } else {
            player.handleMessage(message);
        }
    }

    private void sendKeepAlive() {
        try {
            send("ping");
            LogMaker.info("Ping enviado.");
        } catch (Exception e) {
            LogMaker.error("Erro ao enviar o ping: " + e.getMessage());
        }
    }

    public void disconnect() {
        LogMaker.info("Cliente desconectado.");
        keepAliveEnabled = false;
        super.disconnect();
    }

    public boolean isAlive() {
        return keepAliveEnabled;
    }
}
