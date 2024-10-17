package org.server;

import org.shared.BingoCard;
import org.shared.logs.LogMaker;
import org.shared.messages.*;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RoomServer implements Runnable  {
    private final Integer RANGE_DRAW = 75;
    private final Integer INTERVALms = 3000;
    private final int id;
    private final String name;
    private final HashSet<Integer> drawnNumbers = new HashSet<>();
    private final LinkedList<Integer> tableNumbers = new LinkedList<>();
    private final List<PlayerServer> players = Collections.synchronizedList(new ArrayList<>());
    private final List<BingoMessage> receivedBingos = Collections.synchronizedList(new ArrayList<>());
    private static int roomCounter = 1000; // Inicializa o ID das salas a partir de 1000 é estático para toda instância somar um

    private boolean isActive = false;
    private PlayerServer winner;

    // Lock para gerenciar a sincronização
    private final Lock lock = new ReentrantLock();
    private final Condition canStartCondition = lock.newCondition();

    public RoomServer(String name) {
        this.id = roomCounter++;
        this.name = name;
    }

    public void run() {
        lock.lock();
        try {
            while (!isActive) {
                canStartCondition.await(); // Espera até que isActive seja true
            }
            while (isActive) {
                Thread.sleep(INTERVALms); // Intervalo entre sorteios
                drawNumber();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogMaker.error("Erro na sala: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public Integer getId() {
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
    // Remove jogador na sala
    public void removePlayer(LeaveRoomMessage data) throws Exception {
        Iterator<PlayerServer> iterator = players.iterator();
        if(data.roomId() != id) return;
        while (iterator.hasNext()) {
            PlayerServer player = iterator.next();
            if (player.getId().equals(data.playerId())) {
                iterator.remove(); //remove ele da lista de players
                if(players.isEmpty()) resetRoom();
                return;
            }
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
        if (tableNumbers.isEmpty()) isActive = false;
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
            LogMaker.info("VALIDANDO VENCEDOR");
            receivedBingos.sort(Comparator.comparing(BingoMessage::bingoTime)); //ordena pelo tempo pois o primeiro que enviou que ganha
            Iterator<BingoMessage> iterator = receivedBingos.iterator();
            while (iterator.hasNext()) {
                BingoMessage receivedBingo = iterator.next();
                if (bingoMessageIsValid(receivedBingo)) {
                    PlayerServer winner = findPlayer(receivedBingo.playerId());
                    if (winner != null) return winner;
                } else {
                    // Se for inválido, remove da lista
                    LogMaker.info("Vencedor " + receivedBingo.playerId() + " inválido, removendo da lista.");
                    iterator.remove();
                }
            }
        }
        return null;
    }

    public PlayerServer findPlayer(UUID idPlayer) {
        return players.stream().filter(player -> player.getId().equals(idPlayer)).findFirst().orElse(null);
    }

    private boolean bingoMessageIsValid(BingoMessage bingo) {
        return bingo.card()!= null && bingo.card().getIdRoom() == id && verifyCard(bingo.card());
    }

    private boolean verifyCard(BingoCard card) {
        for (Integer cardNumber : card.getCardNumbers()) {
            if (!drawnNumbers.contains(cardNumber)) return false;
        }
        return true;
    }

    public void resetRoom() {
        lock.lock();
        try {
            isActive = false;
            players.clear();
            drawnNumbers.clear();
            tableNumbers.clear();
            receivedBingos.clear();
            winner = null;

            LogMaker.info("Sala " + getName() + " foi reiniciada.");
        } finally {
            lock.unlock();
        }
    }

    public boolean isActive(){
        return isActive;
    }
    private void stopRoom() {
        isActive = false;
        resetRoom();     // Reinicia a sala para permitir um novo jogo
    }


    //Coloca o número sorteado no protocolo e envia para todos os jogadores da sala
    public void broadcastDrawNumber(int drawNumber) {
        MessageProtocol mensagem = new MessageProtocol(MessageType.SORTEIO, new DrawNumberMessage(this.id, drawNumber));
        ServerUtils.broadcast(this.players, mensagem);
    }

    public void broadcastStartGame() {
        MessageProtocol mensagem = new MessageProtocol(MessageType.AVISO_INICIO_SORTEIO,
                new RoomMessage(id, name));
        ServerUtils.broadcast(this.players, mensagem);
    }

    public void broadcastWinner(PlayerServer winner) {
        MessageProtocol message = new MessageProtocol(MessageType.VENCEDOR, new WinnerMessage(id, winner.getName(), winner.getId()));
        ServerUtils.broadcast(this.players, message);
    }

    public void receiveBingos(BingoMessage message) {
        receivedBingos.add(message);
        LogMaker.info("Bingo recebido do jogador: " + message.playerId());
        verifyWin();
    }
}
