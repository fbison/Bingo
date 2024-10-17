package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Pede para sair da sala
 */
public record LeaveRoomMessage(
        int roomId, UUID playerId
) implements Serializable {
}
