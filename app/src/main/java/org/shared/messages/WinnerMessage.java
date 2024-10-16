package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Comunica o número sorteado
 */
public record WinnerMessage(
        int roomId, String winnerName, UUID idClient
) implements Serializable {
}
