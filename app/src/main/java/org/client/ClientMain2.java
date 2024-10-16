package org.client;

import java.util.Scanner;

public class ClientMain2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String host = "127.0.0.1";
        int port = 12345;

        // Inicializa o cliente de bingo
        BingoClient client = new BingoClient();
        client.connectToServer(host, port);

        boolean running = true;

        while (running) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Registrar");
            System.out.println("2. Login");
            System.out.println("3. Entrar em uma sala");
            System.out.println("4. Enviar Bingo");
            System.out.println("5. Sair");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Digite seu name de usuário:");
                    String registerUsername = scanner.nextLine();
                    System.out.println("Digite sua senha:");
                    String registerPassword = scanner.nextLine();
                    client.register(registerUsername, registerPassword);
                    break;

                case "2":
                    System.out.println("Digite seu name de usuário:");
                    String loginUsername = scanner.nextLine();
                    System.out.println("Digite sua senha:");
                    String loginPassword = scanner.nextLine();
                    client.login(loginUsername, loginPassword);
                    break;

                case "3":
                    System.out.println("Digite o ID da sala que você deseja entrar:");
                    String roomId = scanner.nextLine();
                    client.enterRoom(roomId);
                    break;

                case "4":
                    System.out.println("Enviando Bingo!");
                    client.sendBingo();
                    break;

                case "5":
                    running = false;
                    client.disconnect();
                    break;

                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }

        System.out.println("Cliente encerrado.");
        scanner.close();
    }
}
