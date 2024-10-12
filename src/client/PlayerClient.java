package client;
import common.BingoCard;
import common.JsonParser;
import common.Messages.MessageProtocol;

import java.util.List;

public class PlayerClient {
    private String id;
    private String name;
    private boolean winner;
    private List<BingoCard> cards;

    public void markNumber(int number) {
        // Implementação
    }

    public void chooseRoom() {
        // Implementação
    }

    public void sendBingo() {
        // Implementação
    }

    public void getCards(int number) {
        // Implementação
    }

    public void handleMessage(String originalMessage) {
        MessageProtocol message;
        try {
            message = JsonParser.parseJson(originalMessage, MessageProtocol.class);
        }catch (Exception e){
            System.out.println("Mensagem em formato errado");
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
                System.out.println("Tipo de mensagem não reconhecido.");
                break;
        }
    }
    private void handleSorteio(Object data) {
        // Lógica para o sorteio
        System.out.println("Tratando sorteio: " + data);
    }

    private void handleVencedor(Object data) {
        // Lógica para o vencedor
        System.out.println("Tratando informação de vencedor: " + data);
    }

    private void handleSalasDisponiveis(Object data) {
        // Lógica para salas disponíveis
        System.out.println("Tratando consulta de salas disponíveis: " + data);
    }

    private void handleAvisoInicioSorteio(Object data) {
        // Lógica para aviso de início do sorteio
        System.out.println("Tratando aviso de início do sorteio: " + data);
    }
}
