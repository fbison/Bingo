package org.client;

import java.util.ArrayList;
import java.util.List;

import org.shared.messages.RoomMessage;

public class RoomClient{
    private int id;
    private String name;
    private List<Integer> drawnNumbers;
    private boolean isActive;

    public RoomClient(int id, String name) {
        this.id = id;
        this.name = name;
        this.drawnNumbers = new ArrayList<>();
        this.isActive = false;
    }


    public void receiveNumber(int number) {
        drawnNumbers.add(number);
        System.out.println("Número recebido na sala: " + number);
        displayNewNumber(number); // Atualiza a interface para mostrar o novo número
    }

    // Atualiza a interface com os números sorteados
    public void displayNewNumber(int drawNumber) {
        System.out.println("///////////////////////////");
        System.out.println("///////// SORTEADO ////////" );
        System.out.println("///////// "+ drawNumber +" ////////" );
        System.out.println("///////////////////////////: ");
    }

    // Atualiza a interface com os números sorteados
    public void displayRoomStartes(int drawNumber) {
        System.out.println("///////////////////////////");
        System.out.println("///////// COMEÇOU /////////" );
        System.out.println("///////////////////////////: ");
    }
    // Mostra quais as salas disponíveis
    public void displayRoomsAvailables(List<RoomMessage> rooms) {
        System.out.println("///////////////////////////");

        for (RoomMessage room : rooms) {
            System.out.println("////////// SALA"+ room.roomId()+"/////////" );
            System.out.println("////////// "+ room.name()+"/////////" );
        }
        System.out.println("///////////////////////////: ");
    }

    // Funções para obter as informações da sala, como ID e name
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
