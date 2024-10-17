package org.server;

import org.shared.CommunicationBase;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.MessageProtocol;
import org.shared.messages.MessageType;

import java.net.Socket;

public class ServerCommunication extends CommunicationBase implements Runnable {

    //diminuir depois
    private static final long KEEP_ALIVE_INTERVALms = 15000; // Intervalo para envio do keep-alive (20 segundos)
    private static final long TIMEOUTms = 30000; // Timeout de 30 segundos se não houver resposta

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
                LogMaker.info("Dados recebidos: " + receivedData.toString());
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
                    if (currentTime - lastPongReceivedTime > TIMEOUTms) {
                        LogMaker.info("Timeout: cliente "+ player.getId() +" não respondeu ao keep-alive.");
                        disconnect();
                        break;
                    }
                    sendKeepAlive();
                    Thread.sleep(KEEP_ALIVE_INTERVALms);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        keepAliveThread.start();
    }

    private void processReceivedData(Object receivedData) {
        if (!(receivedData instanceof MessageProtocol messageProtocol)) {
            LogMaker.info("Tipo de mensagem desconhecido.");
            return;
        }

        player.handleMessage(messageProtocol);
    }
    public void handlePong(){
        lastPongReceivedTime = System.currentTimeMillis();
        LogMaker.info("Pong recebido de :" + player.getId());
    }
    private MessageProtocol formatMessage(String originalMessage){
        MessageProtocol message;
        try {
            message = JsonParser.parseJson(originalMessage, MessageProtocol.class);
        } catch (Exception e) {
            LogMaker.info("Mensagem em formato errado");
            return null;
        }
        return  message;
    }
    private void sendKeepAlive() {
        try {
            send(new MessageProtocol(MessageType.PING, null));
            LogMaker.info("Ping enviado para "+player.getId());
        } catch (Exception e) {
            LogMaker.error("Erro ao enviar o ping: " + e.getMessage());
        }
    }

    public void disconnect() {
        LogMaker.info("Cliente desconectado.");
        Server.onlinePlayers.remove(player.getName());
        Server.isOnline.put(player.getName(), false);

        keepAliveEnabled = false;
        super.disconnect();
    }

    public boolean isAlive() {
        return keepAliveEnabled;
    }
}
