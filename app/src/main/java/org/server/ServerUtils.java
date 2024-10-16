package org.server;

import org.shared.messages.MessageProtocol;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerUtils {
        private static final ExecutorService executor = Executors.newFixedThreadPool(10);

        //Faz broadcast para uma lista de players,
        // porém limitando para não abrir um número máximo de threads pesando demais a conexão
        public static void broadcast(List<PlayerServer> players, MessageProtocol message) {
            for (PlayerServer player : players) {
                executor.submit(() -> player.send(message));
            }
        }

}
