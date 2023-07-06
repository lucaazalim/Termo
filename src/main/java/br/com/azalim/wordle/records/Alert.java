package br.com.azalim.wordle.records;

import br.com.azalim.wordle.protocol.Packet;
import br.com.azalim.wordle.states.AlertType;

public record Alert(String message, AlertType type) {

    public void send(Match match) {
        Packet<Alert> wordGridPacket = new Packet<>("alert", match.getUuid(), this);
        wordGridPacket.send(match.getSession());
    }

}
