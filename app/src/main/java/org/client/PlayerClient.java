package org.client;

import org.shared.BingoCard;
import org.shared.JsonParser;
import org.shared.logs.LogMaker;
import org.shared.messages.MessageProtocol;

import java.util.List;
import java.util.Map;

public class PlayerClient {
    private String id;
    private String name;
    private boolean winner;
    private List<BingoCard> cards;

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
                handleSorteio(message.data());
                break;
            case VENCEDOR:
                handleVencedor(message.data());
                break;
            case SALAS_DISPONIVEIS:
                handleSalasDisponiveis(message.data());
                break;
            case AVISO_INICIO_SORTEIO:
                handleAvisoInicioSorteio(message.data());
                break;
            default:
                LogMaker.info("Tipo de mensagem não reconhecido.");
                break;
        }
    }

    private void handleSorteio(Object data) {
        LogMaker.info("Número sorteado: " + data);
        int number = (int) data;
        markNumber(number);
    }

    private void handleVencedor(Object data) {
        LogMaker.info("Vencedor: " + data);
        winner = true;
    }

    private void handleSalasDisponiveis(Object data) {
        LogMaker.info("Salas disponíveis: " + data);

        List<Map<String, Object>> roomsList = JsonParser.parseJson(data.toString(), List.class);
        if (roomsList == null || roomsList.isEmpty()) {
            LogMaker.warn("Nenhuma sala disponível.");
            return;
        }

        for (Map<String, Object> room : roomsList) {
            LogMaker.info("Sala ID: " + room.get("id") + " | Nome: " + room.get("name"));
        }
    }

    private void handleAvisoInicioSorteio(Object data) {
        LogMaker.info("Início do sorteio: " + data);
    }
}
