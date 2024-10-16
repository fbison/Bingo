package org.shared;

import org.shared.logs.LogMaker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BingoCard {
    private final int TOTAL_CARD_NUMBERS = 25; // Exemplo: total de números na cartela
    private int rangeDrawn; // Range dos números a serem sorteados
    private final List<Integer> numbers; // Números da cartela
    private int idRoom; // ID da sala
    private Set<Integer> markedNumbers; // Números marcados

    // Construtor que gera números aleatórios para a cartela
    public BingoCard(Integer range) {
        this.rangeDrawn = range;
        this.numbers = generateRandomCard(range);
        this.markedNumbers = new HashSet<>();
    }

    public List<Integer> getCardNumbers(){
        return  numbers;
    }
    // Gera uma cartela com números aleatórios
    private List<Integer> generateRandomCard(int range) {
        List<Integer> cardNumbers = new ArrayList<>();
        Random random = new Random();
        while (cardNumbers.size() < TOTAL_CARD_NUMBERS) {
            int number = random.nextInt(range) + 1; // Gera números de 1 ao range
            if (!cardNumbers.contains(number)) { // Garante que não haja duplicatas
                cardNumbers.add(number);
            }
        }
        return cardNumbers;
    }

    // Método para imprimir a cartela
    public void printCard() {
        System.out.println("Cartela:");
        for (int i = 0; i < numbers.size(); i++) {
            if (markedNumbers.contains(numbers.get(i))) {
                System.out.print("X ");
            } else {
                System.out.printf("%2d ", numbers.get(i));
            }
            if ((i + 1) % 5 == 0) { // Quebra a linha a cada 5 números
                System.out.println();
            }
        }
        System.out.println();
    }

    // Método para marcar um número na cartela
    public void markNumber(int number) {
        if (numbers.contains(number)) {
            markedNumbers.add(number); // Adiciona o número ao conjunto de números marcados
            LogMaker.info("Número " + number + " marcado na cartela.");
            printCard();
        } else {
            LogMaker.warn("Número " + number + " não está na cartela.");
        }
    }

    // Verifica se todos os números da cartela foram marcados
    public boolean isComplete() {
        return markedNumbers.containsAll(numbers);
    }

    public int getIdRoom() {
        return idRoom;
    }
}
