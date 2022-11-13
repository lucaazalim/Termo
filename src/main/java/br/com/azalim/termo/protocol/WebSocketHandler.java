package br.com.azalim.termo.protocol;

import br.com.azalim.termo.Main;
import br.com.azalim.termo.records.MatchSettings;
import br.com.azalim.termo.records.*;
import br.com.azalim.termo.states.LetterState;
import br.com.azalim.termo.states.MatchState;
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

        UUID uuid = UUID.randomUUID();
        MATCHES.put(uuid, new Match(uuid, session));

        Packet<UUID> handshakePacket = new Packet<>("handshake", uuid, null);
        handshakePacket.send(session);

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
                MatchWord matchWord = new MatchWord(Main.getWord(matchSettings.wordLength()));

                match.setSettings(matchSettings);
                match.setWord(matchWord);

                System.out.println("Match word: " + matchWord.word());

            }

            case "newGuess" -> {

                int matchWordLength = match.getSettings().wordLength();
                Guess guess = ((Packet<Guess>) Main.GSON.fromJson(message, new TypeToken<Packet<Guess>>(){}.getType())).content();

                String guessWord = guess.word();

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

                Word word = new Word(letters);
                WordGrid matchWordGrid = match.getWordGrid();

                Word[] words = Arrays.copyOf(matchWordGrid.words(), matchWordGrid.words().length + 1);

                if(words.length == match.getSettings().maxGuesses()) {
                    state = MatchState.OUT_OF_GUESSES;
                }

                words[words.length - 1] = word;

                matchWordGrid = new WordGrid(words, state);

                match.setWordGrid(matchWordGrid);
                System.out.println(matchWordGrid.asString());

                Packet<WordGrid> wordGridPacket = new Packet<>("word_grid", uuid, matchWordGrid);
                wordGridPacket.send(session);

            }

        }

    }

}