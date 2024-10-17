package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Comunica o número sorteado
 */
public record EnteredRoomMessage(
        int roomId, String roomName, UUID playerId, Boolean isActive
) implements Serializable {
}
