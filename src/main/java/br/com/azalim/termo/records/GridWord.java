package br.com.azalim.termo.records;

import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public record GridWord(Letter[] letters) {

    public String word() {
        return Arrays.stream(letters)
                .map(Letter::letter)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

}
