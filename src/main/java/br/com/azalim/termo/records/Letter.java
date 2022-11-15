package br.com.azalim.termo.records;

import br.com.azalim.termo.states.LetterState;

import java.util.Objects;

public record Letter(char letter, LetterState state) {

}
