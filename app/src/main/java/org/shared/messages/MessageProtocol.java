package org.shared.messages;

import java.io.Serializable;

public record MessageProtocol(MessageType type, Object data) implements Serializable {
}
