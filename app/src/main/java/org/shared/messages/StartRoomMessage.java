package org.shared.messages;

import java.io.Serializable;

/**
 * Comunica que o jogo da sala começou
 */
public record StartRoomMessage(
        int roomId
) implements Serializable {
}
