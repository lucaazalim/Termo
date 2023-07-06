package br.com.azalim.wordle;

import br.com.azalim.wordle.protocol.WebSocketHandler;
import br.com.azalim.wordle.records.Word;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static spark.Spark.*;

public class Main {

    public static Gson GSON = new Gson();
    public static Multimap<Integer, Word> WORDS_BY_LENGTH = MultimapBuilder.hashKeys().arrayListValues().build();
    public static Map<String, Word> WORDS_BY_NORMALIZED = Maps.newHashMap();

    public static void main(String[] args) throws IOException {

        port(80);
        staticFileLocation("/public");
        webSocket("/socket", WebSocketHandler.class);

        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace();
            halt(500);
        });

        loadWords();

        init();

    }

    private static void loadWords() throws IOException {
        URL url = Resources.getResource("br-utf8.txt");
        Arrays.stream(Resources.toString(url, StandardCharsets.UTF_8).split("\n"))
                .forEach(rawWord -> {

                    Word word = new Word(rawWord, Util.normalizeString(rawWord));

                    // Remove palavras iguais com acentos diferentes
                    if(WORDS_BY_NORMALIZED.containsKey(word.normalizedWord())) {
                        return;
                    }

                    WORDS_BY_LENGTH.put(rawWord.length(), word);
                    WORDS_BY_NORMALIZED.put(word.normalizedWord(), word);

                });
    }

    public static Word getRandomWord(int length) throws IOException {
        List<Word> lengthWords = WORDS_BY_LENGTH.get(length).stream().toList();
        return lengthWords.get(new Random().nextInt(lengthWords.size()));
    }

}