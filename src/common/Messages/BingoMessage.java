package common.Messages;

import common.BingoCard;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @param bingoTime Usando OffsetDateTime para não haver problemas de fuso horários diferentes
 */
public record BingoMessage(UUID playerId, UUID roomId, BingoCard card, OffsetDateTime bingoTime) {
}
