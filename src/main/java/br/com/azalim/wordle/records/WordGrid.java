package br.com.azalim.wordle.records;

import br.com.azalim.wordle.states.MatchState;

public record WordGrid(GridWord[] words, MatchState state) {

    public String asString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (GridWord gridWord : words) {
            for (Letter letter : gridWord.letters()) {

                switch (letter.state()) {
                    case IN_POSITION -> stringBuilder.append("✅");
                    case OUT_OF_POSITION -> stringBuilder.append("\uD83D\uDFE1");
                    case NOT_PRESENT -> stringBuilder.append("\uD83D\uDD35");
                }

                stringBuilder.append(letter.letter());
                stringBuilder.append(" ");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();

    }

}
