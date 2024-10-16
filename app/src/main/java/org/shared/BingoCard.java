package org.shared;

import org.shared.logs.LogMaker;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BingoCard {
    private List<Integer> numbers;
    private int idRoom;
    private Set<Integer> markedNumbers;

    public List<Integer> getCardNumbers() {
        return numbers;
    }

    // Método para marcar um número na cartela
    public void markNumber(int number) {
        if (numbers.contains(number)) {
            markedNumbers.add(number); // Adiciona o número ao conjunto de números marcados
            LogMaker.info("Número " + number + " marcado na cartela.");
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
