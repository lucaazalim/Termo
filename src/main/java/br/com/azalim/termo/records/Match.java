package br.com.azalim.termo.records;

import br.com.azalim.termo.states.MatchState;
import lombok.Data;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

@Data
public class Match {

    private final UUID uuid;
    private final Session session;
    private MatchSettings settings;
    private MatchWord word;
    private WordGrid wordGrid = new WordGrid(new Word[]{}, MatchState.IN_PROGRESS);

}
