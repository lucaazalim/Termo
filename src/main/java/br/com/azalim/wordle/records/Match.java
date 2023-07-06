package br.com.azalim.wordle.records;

import br.com.azalim.wordle.states.MatchState;
import lombok.Data;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

@Data
public class Match {

    private final UUID uuid;
    private final Session session;
    private MatchSettings settings;
    private Word word;
    private WordGrid wordGrid = new WordGrid(new GridWord[]{}, MatchState.IN_PROGRESS);

}
