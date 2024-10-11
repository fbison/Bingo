package common.Messages;

import common.BingoCard;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BingoMessage {
    private UUID playerId;
    private UUID roomId;
    private BingoCard card;
    //Usando OffsetDateTime para não houver problemas de fuso horários diferentes
    private OffsetDateTime bingoTime;


    public BingoCard getCard() {
        return card;
    }

    public OffsetDateTime getBingoTime() {
        return bingoTime;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}
