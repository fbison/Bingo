package org.client;

import java.util.ArrayList;
import java.util.List;

import org.shared.messages.RoomMessage;
import org.shared.messages.WinnerMessage;

public class RoomClient{
    private int id;
    private String name;
    private List<Integer> drawnNumbers;
    private Integer lastDrawNumber;
    private boolean isActive;
    private WinnerMessage winnerMessage;
    public RoomClient(int id, String name, boolean isActive) {
        this.id = id;
        this.name = name;
        this.drawnNumbers = new ArrayList<>();
        this.isActive = isActive;
        lastDrawNumber = null;
    }


    public void receiveNumber(int number) {
        lastDrawNumber = number;
        drawnNumbers.add(number);
        System.out.println("Número recebido na sala: " + number);
        //displayNewNumber(number); // Atualiza a interface para mostrar o novo número
    }


    public void handleWinner(WinnerMessage message) {
        this.winnerMessage = message;
    }

    // Funções para obter as informações da sala, como ID e name
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getLastDrawNumber() {
        return lastDrawNumber;
    }
    public List<Integer> getDrawnNumbers() {
        return drawnNumbers;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean state){
        isActive= state;
    }

    public WinnerMessage getWinnerMessage() {
        return winnerMessage;
    }
}
