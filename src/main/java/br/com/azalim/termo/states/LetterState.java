package br.com.azalim.termo.states;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LetterState {

    NOT_PRESENT(),
    OUT_OF_POSITION(),
    IN_POSITION();

}
