package org.shared.messages;

import java.util.UUID;

/**
 * Comunica o número sorteado
 */
public record DrawNumberMessage(
        int roomId,
        int drawNumber
) {
}
