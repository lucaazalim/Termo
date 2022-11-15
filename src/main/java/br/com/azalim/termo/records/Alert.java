package br.com.azalim.termo.records;

import br.com.azalim.termo.protocol.Packet;
import br.com.azalim.termo.states.AlertType;

public record Alert(String message, AlertType type) {

    public void send(Match match) {
        Packet<Alert> wordGridPacket = new Packet<>("alert", match.getUuid(), this);
        wordGridPacket.send(match.getSession());
    }

}
