package org.shared.messages;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Comunica o número sorteado
 */
public record RoomsMessage(
        ArrayList<RoomMessage> rooms
) implements Serializable {
}
