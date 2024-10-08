package common;

import java.io.*;
import java.net.Socket;

public abstract class CommunicationBase {
    protected Socket socket;

    protected CommunicationBase(Socket socket) {
        this.socket = socket;
    }
    public void send(Object data) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Erro ao enviar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public Object receive() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao receber dados: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null em caso de erro
    }

    public void connect(String host, int port) {
        try {
            // Estabelece uma conexão com o host e a porta fornecidos
            socket = new Socket(host, port);
            System.out.println("Conectado ao servidor em " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Fecha o socket
                System.out.println("Conexão encerrada.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
