package server;

import common.BingoCard;
import java.util.*;

import static java.util.UUID.randomUUID;

public class RoomServer {
    private final Integer RANGE_DRAW = 75;
    private UUID id = randomUUID();
    private List<PlayerServer> players = new ArrayList<>();
    private List<Integer> tableNumbers = new ArrayList<>();
    private boolean isActive = false;
    private final HashSet<Integer> drawnNumbers = new HashSet<Integer>();


    public void addPlayer(PlayerServer player) {
        players.add(player);
    }

    private void setupTableNumbers(){
        for (int i = 1; i <= RANGE_DRAW; i++) {
            tableNumbers.add(i);
        }
        Collections.shuffle(tableNumbers);
    }

    public void startGame() {
        if(players.size() <= 1) return;
        isActive = true;
        setupTableNumbers();
        broadcastStartGame();
    }

    public void drawNumber() throws Exception {
        if (tableNumbers.isEmpty()) throw new Exception("Numeros da sala já terminaram");
        int drawNumber = tableNumbers.removeFirst();
        drawnNumbers.add(drawNumber);
        broadcastDrawNumber(drawNumber);
    }

    public void receiveBingo() {
        // Implementação
    }

    private boolean verifyCard(BingoCard card) {
        for (Integer cardNumber : card.getCardNumbers()) {
            if(!tableNumbers.contains(cardNumber)) return false;
        }
        return true;
    }

    public void verifyWinner(int playerId, BingoCard card) {
        broadcastWinner(playerId);
    }

    public void broadcastDrawNumber(int drawNumber) {
        // Implementação
    }

    public void broadcastStartGame() {
        // Implementação
    }

    public void broadcastWinner(int winnerId) {
        // Implementação
    }
}
