package org.shared.messages;

/**
 * Comunica que o jogo da sala começou
 */
public record StartRoomMessage(
        int roomId
) {
}
