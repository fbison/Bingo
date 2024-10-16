package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Comunica o número sorteado
 */
public record DrawNumberMessage(
        int roomId,
        int drawNumber
) implements Serializable {
}
