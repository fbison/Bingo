package org.shared.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Utilizado para o register e o login
 */
public record AuthenticationMessage(
        String username, String password
) implements Serializable {}
