package server;

import common.CommunicationBase;

import java.net.Socket;

public class ServerCommunication extends CommunicationBase implements Runnable{

    public ServerCommunication(Socket clientSocket){
        super(clientSocket);
    }
    // O que será executado na thread
    @Override
    public void run() {
        handleClient();
    }

    // Método que gerencia a comunicação com o cliente (recebimento de dados)
    public void handleClient() {
        try {
            while (true) {
                Object receivedData = receive(); // Seria isso o keepAlive?

                if (receivedData == null) {
                    //Quando o cliente se desconectar, ele precisa ser retirado da
                    // classe do PlayersOnline tanto no server quando no Room
                    // talvez possa enviar uma exceção no disconect e o próprio starServer desconecta

                    System.out.println("Cliente desconectado.");
                    disconnect(); // Desconecta ao receber null (cliente desconectado)
                    break;
                }
                System.out.println("Mensagem recebida: " + receivedData);
            }
        } catch (Exception e) {
            System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect(); // Garantir que a conexão seja encerrada
        }
    }
}
