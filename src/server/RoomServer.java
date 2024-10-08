package server;

import common.BingoCard;
import common.CommunicationBase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.*;

import static java.util.UUID.randomUUID;

public class RoomServer {
    private final Integer RANGE_DRAW = 75;
    private final UUID id = randomUUID();
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

    // Broadcast para players (duplicado no Server e Room Server) talvez colocar em alguma biblioteca Util que receba uma lista de players com as mensagens
    public void broadcast(Object object) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (PlayerServer player : players) {
                //no protocolo é preciso alterar as partes relacionadas ao usuário, fazer isso quando finalizado
                executor.submit(() -> {
                    player.send(object);
                });
            }
        }
    }

    // Broadcast para o sorteio enviado
    public void broadcastDrawNumber(int drawNumber) {
        broadcast("Número sorteado: " + drawNumber);
    }

    // Broadcast para iniciar o jogo
    public void broadcastStartGame() {
        broadcast("O jogo começou!");
    }

    // Broadcast para informar quem venceu
    public void broadcastWinner(int winnerId) {
        broadcast("O jogador com ID " + winnerId + " venceu!");
    }
}
