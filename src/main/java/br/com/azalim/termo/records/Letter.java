package br.com.azalim.termo.records;

import br.com.azalim.termo.states.LetterState;

public record Letter(
        char letter, LetterState state) {

}
