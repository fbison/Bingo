package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Retorna informações de log in confirmando o sucesso
 */
public record LogInReturnMessage(
        UUID playerId, String name
) implements Serializable {}
