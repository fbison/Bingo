package org.shared;

import org.shared.logs.LogMaker;
import org.shared.messages.MessageProtocol;
import org.shared.messages.MessageType;

import java.io.*;
import java.net.Socket;

public abstract class CommunicationBase {
    protected Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    protected CommunicationBase(Socket socket) {
        this.socket = socket;
        initializeStreams();
    }

    /**
     * Inicializa os fluxos de entrada e saída para o socket.
     */
    private void initializeStreams() {
        try {
            if (socket != null && !socket.isClosed()) {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                LogMaker.info("Streams de entrada e saída inicializados com sucesso.");
            }
        } catch (IOException e) {
            LogMaker.error("Erro ao inicializar streams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envia dados através do socket.
     */
    //trocar para MessageProtocol o parâmetro
    public void send(Object data) {
        if (outputStream == null) {
            LogMaker.error("Erro ao enviar dados: OutputStream não está inicializado.");
            return;
        }

        try {
            outputStream.writeObject(data);
            outputStream.flush();
        } catch (IOException e) {
            LogMaker.error("Erro ao enviar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recebe dados através do socket.
     */
    public Object receive() {
        if (inputStream == null) {
            LogMaker.error("Erro ao receber dados: InputStream não está inicializado.");
            return null;
        }

        try {
            Object data = inputStream.readObject();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            LogMaker.error("Erro ao receber dados: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null em caso de erro
    }

    /**
     * Conecta a um servidor especificado pelo host e porta.
     */
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            LogMaker.info("Conectado ao servidor em " + host + ":" + port);
            initializeStreams();  // Recria os fluxos após a conexão ser estabelecida
        } catch (IOException e) {
            LogMaker.error("Erro ao conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Desconecta o socket e fecha os fluxos de entrada e saída.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Fecha o socket
                LogMaker.info("Conexão encerrada.");
            }

            if (outputStream != null) {
                outputStream.close(); // Fecha o outputStream
                LogMaker.info("OutputStream fechado.");
            }

            if (inputStream != null) {
                inputStream.close(); // Fecha o inputStream
                LogMaker.info("InputStream fechado.");
            }

        } catch (IOException e) {
            LogMaker.error("Erro ao desconectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
