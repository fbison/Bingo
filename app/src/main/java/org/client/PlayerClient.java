package org.client;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerClient {
    private final UUID id;
    private String name;
    private boolean winner;
    private RoomClient currentRoom;
    private List<BingoCard> cards;
    private List<RoomMessage> availableRooms;
    private ClientCommunication clientCommunication;

    public PlayerClient(UUID id, String name, ClientCommunication communication) {
        this.id = id;  // Gera ID numérico entre 1000 e 9999
        this.name = name;
        this.winner = false;
        this.clientCommunication = communication;
    }

    public UUID getId() {
        return id;
    }

    public void markNumber(int number) {
        if (cards != null) {
            cards.forEach(card -> card.markNumber(number));
        } else {
            System.out.println("Nenhuma cartela disponível.");
        }
    }

    public void handleMessage(MessageProtocol message) {

        switch (message.type()) {
            case SORTEIO:
                handleSorteio((DrawNumberMessage) message.data());
                break;
            case VENCEDOR:
                handleVencedor(message.data());
                break;
            case SALAS_DISPONIVEIS:
                handleSalasDisponiveis((RoomsMessage)message.data());
                break;
            case AVISO_INICIO_SORTEIO:
                handleAvisoInicioSorteio((RoomMessage)message.data());
                break;
            default:
                LogMaker.info("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    private void handleSorteio(DrawNumberMessage data) {
        if(getCurrentRoom() == null || getCurrentRoom().getId() != data.roomId()) return; // Só ignora a mensagem se não for pra sala que ele está
        LogMaker.info("Número sorteado: " + data);
        getCurrentRoom().receiveNumber(data.drawNumber());
        markNumber(data.drawNumber());
    }

    private void handleVencedor(Object data) {
        LogMaker.info("Vencedor: " + data);
        winner = true;
    }

    private void handleSalasDisponiveis(RoomsMessage data) {
        availableRooms = data.rooms();
    }

    private void handleAvisoInicioSorteio(RoomMessage data) {
        LogMaker.info("Início do sorteio: " + data);
        setCurrentRoom(
                new RoomClient(data.roomId(), data.name())
        );
        Integer RANGE_DRAW = 75;
        cards = new ArrayList<>();
        cards.add(new BingoCard(RANGE_DRAW));
    }

    public RoomClient getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(RoomClient currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void printCards(){
        for (BingoCard card : cards) {
            card.printCard();
        }
    }
}
