package server;

import common.BingoCard;
import common.Messages.BingoMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.*;

import static java.util.UUID.randomUUID;

public class RoomServer {
    private final Integer RANGE_DRAW = 75;
    private final Integer INTERVALms = 1000;

    private final UUID id = randomUUID();
    private final HashSet<Integer> drawnNumbers = new HashSet<>();
    private final LinkedList<Integer> tableNumbers = new LinkedList<>(); // é utilizada pois retirar o primeiro é O(1)
    private final List<PlayerServer> players = Collections.synchronizedList(new ArrayList<>());
    private final List<BingoMessage> receivedBingos = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = Executors.newFixedThreadPool(10);


    private boolean isActive = false;
    private PlayerServer winner;

    // Cria um lock para gerenciar o acesso à seção crítica que é adicionar mais jogadores
    private final Lock lock = new ReentrantLock();
    // Cria uma condição que as threads podem usar para aguardar ou ser notificadas
    private final Condition canStartCondition = lock.newCondition();

    /**
     * Método que espera até que a sala possa começar.
     * As threads que chamam esse método irão esperar até que a condição 'canStart()' seja verdadeira.
     */
    private void waitStart() throws InterruptedException {
        lock.lock(); // Adquire o lock para garantir acesso exclusivo
        try {
            // Enquanto a sala não puder começar, a thread irá esperar
            while (!canStart()) {
                canStartCondition.await(); // Libera o lock e espera ser notificado
            }
            startRoom(); // Inicia a sala se a condição for atendida
        } finally {
            lock.unlock(); // Libera o lock, permitindo que outras threads acessem a seção crítica
        }
    }

    /**
     * Método para adicionar um jogador à sala, somente se a sala não estiver ativa
     * Se a adição do jogador torna a sala capaz de começar, notifica as threads que estão esperando.
     */
    public void addPlayer(PlayerServer player) throws Exception {
        lock.lock(); // Adquire o lock para garantir acesso exclusivo
        try {
            if (isActive) throw new Exception("Não é possível entrar nessa sala pois a sala já começou");
            players.add(player);
            if (canStart()) {
                // Se a adição do jogador permite que a sala comece,
                canStartCondition.signalAll(); // notifica todas as threads que estão esperando
            }
        } finally {
            lock.unlock(); // Libera o lock, permitindo que outras threads acessem a seção crítica
        }
    }
    public void runRoom() throws Exception{
        waitStart();
        while(isActive){
            drawNumber();
            Thread.sleep(INTERVALms);
            verifyWin();
        }
    }

    //Outra thread (a do serverComunication) pode adicionar aqui que vai receber
    public void receiveBingos(BingoMessage message){
        receivedBingos.add(message);
    }

    //popula uma lista com todos os números que serão sorteados e aleatoriamente troca a ordem deles
    // o sorteio será feito nessa ordem
    private void setupTableNumbers(){
        for (int i = 1; i <= RANGE_DRAW; i++) {
            tableNumbers.add(i);
        }
        Collections.shuffle(tableNumbers);
    }
    //fecha o executor (que faz o controle evitando o envio de muitas mensagens ao mesmo tempo)
    public void shutdownExecutor() {
        executor.shutdown();
    }

    private boolean canStart(){
        return players.size() >1;
    }

    public void startRoom() {
        if(isActive && canStart()) return;
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

    //verifica se a cartela do bingo é valida
    private boolean verifyCard(BingoCard card) {
        for (Integer cardNumber : card.getCardNumbers()) {
            if(!drawnNumbers.contains(cardNumber)) return false;
        }
        return true;
    }

    private void stopRoom(){
        isActive = false;
        shutdownExecutor();
    }

    public void verifyWin(){
        PlayerServer winner = findValidWinner();
        if(winner != null) {
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


    public PlayerServer findPlayer(UUID idPlayer){
        return players.stream()
                .filter(player -> player.getId().equals(idPlayer))
                .findFirst()
                .orElse(null);
    }

    public boolean bingoMessageIsValid(BingoMessage bingo) {
        return bingo.card().getIdRoom().equals(id) &&
                verifyCard(bingo.card());
    }

    // Broadcast para o sorteio enviado
    public void broadcastDrawNumber(int drawNumber) {
        ServerUtils.broadcast(this.players, "Número sorteado: " + drawNumber);
    }

    // Broadcast para iniciar o jogo
    public void broadcastStartGame() {
        ServerUtils.broadcast(this.players, "O jogo começou!");
    }

    // Broadcast para informar quem venceu
    public void broadcastWinner(PlayerServer winner) {
        ServerUtils.broadcast(this.players, "O jogador com ID " + winner.getId() + " venceu!");
    }

    public UUID getId() {
        return id;
    }
}
