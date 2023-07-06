package br.com.azalim.wordle.records;

import java.util.Arrays;
import java.util.stream.Collectors;

public record GridWord(Letter[] letters) {

    public String word() {
        return Arrays.stream(letters)
                .map(Letter::letter)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

}
