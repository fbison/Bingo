package org.client;

import org.shared.BingoCard;
import org.shared.logs.LogMaker;
import org.shared.messages.RoomMessage;
import org.shared.messages.WinnerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BingoInterface implements Runnable {
    private final BingoClient bingoClient;
    private final Scanner scanner;
    private boolean running;

    public BingoInterface(BingoClient bingoClient) {
        this.bingoClient = bingoClient;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    @Override
    public void run(){
        while (running) {
            if (bingoClient.getPlayerLoggedIn() == null) {
                showLoginMenu();
            }  else if (bingoClient.getPlayerLoggedIn().getCurrentRoom() == null) {
                showMainMenu();
            } else {
                showRoomInterface();
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("=== MENU DE LOGIN ===");
        System.out.println("1. Registrar");
        System.out.println("2. Logar");
        System.out.print("Escolha uma opção: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consumir nova linha
        String username;
        String password;
        switch (choice) {
            case 1:
                System.out.print("Digite seu nome de usuário: ");
                username = scanner.nextLine();
                System.out.print("Digite sua senha: ");
                password = scanner.nextLine();
                bingoClient.register(username, password);
                break;
            case 2:
                System.out.print("Digite seu nome de usuário: ");
                username = scanner.nextLine();
                System.out.print("Digite sua senha: ");
                password = scanner.nextLine();
                bingoClient.login(username, password);
                System.out.println("Esperando Resposta: ");
                await();
                break;
            default:
                LogMaker.warn("Opção inválida.");
        }
    }
    private void await(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LogMaker.error(e.getMessage());
        }
    }
    private void showMainMenu() {
        System.out.println("=== MENU DE JOGO ===");
        System.out.println("1. Entrar em uma sala");
        System.out.println("2. Deslogar");
        System.out.print("Escolha uma opção: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consumir nova linha

        switch (choice) {
            case 1:
                displayRoomsAvailables();
                System.out.print("Digite o ID da sala: ");
                int roomId = scanner.nextInt();
                bingoClient.enterRoom(roomId);
                await();
                break;
            case 2:
                bingoClient.logOut();
                break;
            default:
                LogMaker.warn("Opção inválida.");
        }
    }

    // Mostra quais as salas disponíveis
    public void displayRoomsAvailables() {
        List<RoomMessage> rooms = bingoClient.getPlayerLoggedIn().getAvailableRooms();
        System.out.println("__________________________");
        for (RoomMessage room : rooms) {
            System.out.println("/ SALA "+ room.roomId()+" N: "+ room.name()+"/////////" );
        }
        System.out.println("___________________________ ");
    }

    private void showRoomInterface(){
        int lastDrawnNumber = -1; // Valor inicial inválido para comparação

        if(bingoClient.getPlayerLoggedIn().getCurrentRoom() != null &&
                !bingoClient.getPlayerLoggedIn().getCurrentRoom().isActive())  {
            System.out.println("Sala não iniciada, aguarde \n");
        }

        while (bingoClient.getPlayerLoggedIn().getCurrentRoom() != null &&
                bingoClient.getPlayerLoggedIn().getCurrentRoom().getWinnerMessage() == null
                ) {

            if(bingoClient.getPlayerLoggedIn().getCurrentRoom().isActive()){
                lastDrawnNumber = roomInterfaceWhileActive(lastDrawnNumber);
            }
            // Sleep por um curto período para evitar uso excessivo de CPU
            try {
                Thread.sleep(100); // Aguarda 100 ms antes de verificar novamente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura o status de interrupção
                break;
            }
        }
        if(bingoClient.getPlayerLoggedIn().getCurrentRoom().getWinnerMessage() != null){
            if(bingoClient.getPlayerLoggedIn().isWinner()){
                winnerParty();
            }else{
                winnerFuneral(bingoClient.getPlayerLoggedIn().getCurrentRoom().getWinnerMessage());
            }
            bingoClient.getPlayerLoggedIn().leaveRoom();
        }

    }
    private void winnerParty(){
        System.out.println("------------------------------");
        System.out.println("|-VOCÊ VENCEU, DEUS TE PAGUE -|");
        System.out.println("------------------------------");
    }
    private void winnerFuneral(WinnerMessage data){
        System.out.println("------------------------------");
        System.out.println("|-VOCÊ PERDEU, DEUS TE AJUDE -|");
        System.out.println("|-"+ data.winnerName()+" teve mais sorte -|");
        System.out.println("------------------------------");
    }
    private int roomInterfaceWhileActive(int lastDrawnNumber){
        // Verifica o número mais recente sorteado
        if (bingoClient.getPlayerLoggedIn().getCurrentRoom().getLastDrawNumber() != null
            && bingoClient.getPlayerLoggedIn().getCurrentRoom().getLastDrawNumber() != lastDrawnNumber) {

            lastDrawnNumber = bingoClient.getPlayerLoggedIn().getCurrentRoom().getLastDrawNumber();

            System.out.println("Número sorteado: " + lastDrawnNumber);
            System.out.println("Números já sorteados: " + bingoClient.getPlayerLoggedIn().getCurrentRoom().getDrawnNumbers());
            bingoClient.getPlayerLoggedIn().printCards();
            System.out.println("Digite um número para marca-lo'B' para gritar Bingo e 'S para sair' :");
        }


        // Espera pela entrada do jogador, sem bloquear o loop
        try {
            if (System.in.available() > 0) {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("B")) {
                    bingoClient.sendBingo(); // Envia a declaração de Bingo
                }else if (input.equalsIgnoreCase("S")) {
                    bingoClient.getPlayerLoggedIn().leaveRoom(); // Envia a declaração de Bingo
                } else {
                    try {
                        int numberToMark = Integer.parseInt(input);
                        bingoClient.getPlayerLoggedIn().markNumber(numberToMark); // Marca o número
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Tente novamente.");
                    }
                }
            }
        }catch (IOException e){
            LogMaker.error(e.getMessage());
        }

        return lastDrawnNumber;
    }

    public void stop() {
        running = false;
    }
}
