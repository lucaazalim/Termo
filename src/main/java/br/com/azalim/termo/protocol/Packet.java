package br.com.azalim.termo.protocol;

import br.com.azalim.termo.Main;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

public record Packet<T>(String type, UUID uuid, T content) {

    public void send(Session session) {
        session.getRemote().sendStringByFuture(Main.GSON.toJson(this));
    }

}
