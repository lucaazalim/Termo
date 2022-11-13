package br.com.azalim.termo;

import br.com.azalim.termo.protocol.WebSocketHandler;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import static spark.Spark.*;

public class Main {

    public static Gson GSON = new Gson();

    public static void main(String[] args) throws IOException {

        port(80);
        staticFileLocation("/public");
        webSocket("/socket", WebSocketHandler.class);

        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace();
            halt(500);
        });

        List<String> words = getWords();

        System.out.println(words.size());

        init();

    }

    public static String getWord(int length) throws IOException {
        List<String> lengthWords = getWords().stream().filter(word -> word.length() == length).toList();
        return lengthWords.get(new Random().nextInt(lengthWords.size()));
    }

    public static List<String> getWords() throws IOException {
        URL url = Resources.getResource("br-utf8.txt");
        return Lists.newArrayList(Resources.toString(url, StandardCharsets.UTF_8).split("\n"));
    }

}