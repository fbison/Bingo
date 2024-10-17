package org.shared.messages;

import java.io.Serializable;

/**
 * Comunica o número sorteado
 */
public record RoomMessage(
        Integer roomId, String name
) implements Serializable {
}
