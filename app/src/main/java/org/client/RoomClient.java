package org.client;

import java.util.ArrayList;
import java.util.List;

public class RoomClient {
    private String id;
    private String name;
    private List<Integer> drawnNumbers;

    public RoomClient(String id, String name) {
        this.id = id;
        this.name = name;
        this.drawnNumbers = new ArrayList<>();
    }

    public void receiveNumber(int number) {
        drawnNumbers.add(number);
        System.out.println("Número recebido na sala: " + number);
        interfaceDisplay(); // Atualiza a interface para mostrar o novo número
    }

    public void interfaceDisplay() {
        // Atualiza a interface com os números sorteados
        System.out.println("Números sorteados até agora: " + drawnNumbers);
    }

    public void handleMessage(String originalMessage) {
        // Método para processar as mensagens recebidas relacionadas à sala
        if (originalMessage.contains("Número sorteado")) {
            // Extrair o número sorteado da mensagem e chamar o método apropriado
            try {
                int number = Integer.parseInt(originalMessage.replaceAll("[^0-9]", ""));
                receiveNumber(number);
            } catch (NumberFormatException e) {
                System.out.println("Erro ao processar número sorteado: " + e.getMessage());
            }
        } else {
            System.out.println("Mensagem não reconhecida pela sala: " + originalMessage);
        }
    }

    // Funções para obter as informações da sala, como ID e nome
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
