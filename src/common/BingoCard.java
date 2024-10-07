package common;

import java.util.List;
import java.util.Set;

public class BingoCard {
    private List<Integer> numbers;
    private int idRoom;
    private Set<Integer> markedNumbers;

    public List<Integer> getCardNumbers(){
        return numbers;
    }
    public void markNumber(int number) {
        // Implementação
    }

    public boolean isComplete() {
        // Implementação
        return false; // Exemplo
    }
}
