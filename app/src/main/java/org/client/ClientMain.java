package org.client;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String host = "127.0.0.1";
        int port = 12345;

        // Inicializa o cliente de bingo
        BingoClient client = new BingoClient();
        client.connectToServer(host, port);

        System.out.println("Cliente encerrado.");
        scanner.close();
    }
}
