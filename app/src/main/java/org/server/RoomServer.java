package org.server;

import org.shared.BingoCard;
import org.shared.logs.LogMaker;
import org.shared.messages.BingoMessage;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RoomServer {
    private static int roomCounter = 1000; // Inicializa o ID das salas a partir de 1000
    private final int id;
    private final String name;
    private final Integer RANGE_DRAW = 75;
    private final Integer INTERVALms = 1000;
    private final HashSet<Integer> drawnNumbers = new HashSet<>();
    private final LinkedList<Integer> tableNumbers = new LinkedList<>();
    private final List<PlayerServer> players = Collections.synchronizedList(new ArrayList<>());
    private final List<BingoMessage> receivedBingos = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private boolean isActive = false;
    private PlayerServer winner;

    // Lock para gerenciar a sincronização
    private final Lock lock = new ReentrantLock();
    private final Condition canStartCondition = lock.newCondition();

    public RoomServer(String name) {
        this.id = roomCounter++;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Adicionar jogador na sala
    public void addPlayer(PlayerServer player) throws Exception {
        lock.lock();
        try {
            if (isActive) throw new Exception("Não é possível entrar nessa sala pois a sala já começou");
            players.add(player);
            if (canStart()) {
                canStartCondition.signalAll();
                startRoom();  // Inicia a sala automaticamente
            }
        } finally {
            lock.unlock();
        }
    }

    // Método para iniciar a sala
    private boolean canStart() {
        return players.size() > 1;
    }

    private void startRoom() {
        if (isActive && canStart()) return;
        isActive = true;
        setupTableNumbers();
        broadcastStartGame();
    }

    private void setupTableNumbers() {
        for (int i = 1; i <= RANGE_DRAW; i++) {
            tableNumbers.add(i);
        }
        Collections.shuffle(tableNumbers);
    }

    public void drawNumber() throws Exception {
        if (tableNumbers.isEmpty()) throw new Exception("Números acabaram");
        int drawNumber = tableNumbers.removeFirst();
        drawnNumbers.add(drawNumber);
        broadcastDrawNumber(drawNumber);
    }

    public void verifyWin() {
        PlayerServer winner = findValidWinner();
        if (winner != null) {
            broadcastWinner(winner);
            stopRoom();
        }
    }

    public PlayerServer findValidWinner() {
        synchronized (receivedBingos) {
            receivedBingos.sort(Comparator.comparing(BingoMessage::bingoTime));
            for (BingoMessage receivedBingo : receivedBingos) {
                if (bingoMessageIsValid(receivedBingo)) {
                    PlayerServer winner = findPlayer(receivedBingo.playerId());
                    if (winner != null) return winner;
                }
            }
        }
        return null;
    }

    public PlayerServer findPlayer(UUID idPlayer) {
        return players.stream().filter(player -> player.getId().equals(idPlayer)).findFirst().orElse(null);
    }

    private boolean bingoMessageIsValid(BingoMessage bingo) {
        return bingo.card().getIdRoom().toString().equals(String.valueOf(id)) && verifyCard(bingo.card());
    }

    private boolean verifyCard(BingoCard card) {
        for (Integer cardNumber : card.getCardNumbers()) {
            if (!drawnNumbers.contains(cardNumber)) return false;
        }
        return true;
    }

    private void stopRoom() {
        isActive = false;
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        executor.shutdown();
    }

    // Funções de broadcast
    public void broadcastDrawNumber(int drawNumber) {
        ServerUtils.broadcast(this.players, "Número sorteado: " + drawNumber);
    }

    public void broadcastStartGame() {
        ServerUtils.broadcast(this.players, "O jogo começou!");
    }

    public void broadcastWinner(PlayerServer winner) {
        ServerUtils.broadcast(this.players, "O jogador " + winner.getId() + " venceu!");
    }

    public void receiveBingos(BingoMessage message) {
        receivedBingos.add(message);
        LogMaker.info("Bingo recebido do jogador: " + message.playerId());
        verifyWin();
    }
}
