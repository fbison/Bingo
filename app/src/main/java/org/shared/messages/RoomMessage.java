package org.shared.messages;

/**
 * Comunica o número sorteado
 */
public record RoomMessage(
        int roomId, String name
) {
}
