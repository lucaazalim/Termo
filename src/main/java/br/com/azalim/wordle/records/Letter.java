package br.com.azalim.wordle.records;

import br.com.azalim.wordle.states.LetterState;

public record Letter(char letter, LetterState state) {

}
