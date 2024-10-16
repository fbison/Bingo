package org.client;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.DrawNumberMessage;
import org.shared.messages.MessageProtocol;
import org.shared.messages.RoomMessage;
import org.shared.messages.RoomsMessage;

import java.util.List;
import java.util.Map;

public class PlayerClient {
    private String id;
    private String name;
    private boolean winner;
    private RoomClient currentRoom;
    private List<BingoCard> cards;
    private List<RoomMessage> availableRooms;
    public PlayerClient(String name) {
        this.id = String.valueOf((int)(Math.random() * 9000) + 1000);  // Gera ID numérico entre 1000 e 9999
        this.name = name;
        this.winner = false;
    }

    public String getId() {
        return id;
    }

    public void markNumber(int number) {
        if (cards != null) {
            cards.forEach(card -> card.markNumber(number));
        } else {
            System.out.println("Nenhuma cartela disponível.");
        }
    }

    public void handleMessage(String originalMessage) {
        MessageProtocol message = JsonParser.parseJson(originalMessage, MessageProtocol.class);
        if (message == null) {
            LogMaker.info("Mensagem em formato errado");
            return;
        }

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
                handleAvisoInicioSorteio(message.data());
                break;
            default:
                LogMaker.info("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    private void handleSorteio(DrawNumberMessage data) {
        LogMaker.info("Número sorteado: " + data);
        if(getCurrentRoom().getId() != data.roomId()) return; // Só ignora a mensagem se não for pra sala que ele está
        markNumber(data.drawNumber());
    }

    private void handleVencedor(Object data) {
        LogMaker.info("Vencedor: " + data);
        winner = true;
    }

    private void handleSalasDisponiveis(RoomsMessage data) {
        availableRooms = data.rooms();
    }

    private void handleAvisoInicioSorteio(Object data) {
        LogMaker.info("Início do sorteio: " + data);
    }

    public RoomClient getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(RoomClient currentRoom) {
        this.currentRoom = currentRoom;
    }
}
