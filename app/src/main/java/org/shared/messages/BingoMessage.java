package org.shared.messages;

import org.shared.BingoCard;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @param bingoTime Usando OffsetDateTime para não haver problemas de fuso horários diferentes
 */
public record BingoMessage(
        UUID playerId,
        int roomId,
        BingoCard card,
        OffsetDateTime bingoTime
) implements Serializable {
}
