package br.com.azalim.wordle.protocol;

import br.com.azalim.wordle.Main;
import br.com.azalim.wordle.Util;
import br.com.azalim.wordle.records.MatchSettings;
import br.com.azalim.wordle.records.*;
import br.com.azalim.wordle.states.AlertType;
import br.com.azalim.wordle.states.LetterState;
import br.com.azalim.wordle.states.MatchState;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@WebSocket
public class WebSocketHandler {

    public static final BiMap<UUID, Match> MATCHES = Maps.synchronizedBiMap(HashBiMap.create());

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {

        System.out.println("Client connected!");
        this.sendHandshake(session);

    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {

    }

    @OnWebSocketMessage
    public void handleTextMessage(Session session, String message) throws IOException {

        if(!session.isOpen()) {
            return;
        }

        Packet<?> packet = Main.GSON.fromJson(message, Packet.class);
        System.out.println("Data received: " + packet.toString());

        UUID uuid = packet.uuid();
        Match match = MATCHES.get(uuid);

        switch (packet.type()) {

            case "gameSettings" -> {

                MatchSettings matchSettings = ((Packet<MatchSettings>) Main.GSON.fromJson(message, new TypeToken<Packet<MatchSettings>>(){}.getType())).content();
                Word matchWord = Main.getRandomWord(matchSettings.wordLength());

                match.setSettings(matchSettings);
                match.setWord(matchWord);

                this.sendWordGrid(match);

                System.out.println("Match word: " + matchWord);

            }

            case "restart" -> this.sendHandshake(session);

            case "newGuess" -> {

                Guess guess = ((Packet<Guess>) Main.GSON.fromJson(message, new TypeToken<Packet<Guess>>(){}.getType())).content();
                String guessWord = guess.word();
                String normalizedGuessWord = Util.normalizeString(guessWord);

                Word word = Main.WORDS_BY_NORMALIZED.get(normalizedGuessWord);

                if(word == null) {
                    new Alert("Esta palavra não existe.", AlertType.WARNING).send(match);
                    return;
                }

                if(Arrays.stream(match.getWordGrid().words()).map(GridWord::word).anyMatch(word.word()::equals)) {
                    new Alert("Você já tentou esta palavra.", AlertType.WARNING).send(match);
                    return;
                }

                guessWord = word.word();
                int matchWordLength = match.getSettings().wordLength();

                if(guessWord.length() != matchWordLength) {
                    return;
                }

                String matchWord = match.getWord().word();
                Letter[] letters = new Letter[matchWordLength];
                MatchState state = MatchState.GUESSED;

                for (int index = 0; index < matchWordLength; index++) {

                    char guessChar = guessWord.charAt(index);
                    char matchWordChar = matchWord.charAt(index);

                    if (guessChar == matchWordChar) {
                        letters[index] = new Letter(matchWordChar, LetterState.IN_POSITION);
                        continue;
                    }

                    if (matchWord.contains(String.valueOf(guessChar))) {
                        letters[index] = new Letter(guessChar, LetterState.OUT_OF_POSITION);
                    } else {
                        letters[index] = new Letter(guessChar, LetterState.NOT_PRESENT);
                    }

                    state = MatchState.IN_PROGRESS;

                }

                GridWord gridWord = new GridWord(letters);
                WordGrid matchWordGrid = match.getWordGrid();

                GridWord[] gridWords = Arrays.copyOf(matchWordGrid.words(), matchWordGrid.words().length + 1);

                if(gridWords.length == match.getSettings().maxGuesses()) {
                    state = MatchState.OUT_OF_GUESSES;
                }

                gridWords[gridWords.length - 1] = gridWord;

                matchWordGrid = new WordGrid(gridWords, state);

                match.setWordGrid(matchWordGrid);
                System.out.println(matchWordGrid.asString());

                this.sendWordGrid(match);

            }

        }

    }

    private void sendHandshake(Session session) {

        UUID uuid = UUID.randomUUID();
        Match match = new Match(uuid, session);

        MATCHES.put(uuid, match);

        Packet<UUID> handshakePacket = new Packet<>("handshake", uuid, null);
        handshakePacket.send(session);

    }

    private void sendWordGrid(Match match) {
        Packet<WordGrid> wordGridPacket = new Packet<>("word_grid", match.getUuid(), match.getWordGrid());
        wordGridPacket.send(match.getSession());
    }

}