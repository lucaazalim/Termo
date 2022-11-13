package br.com.azalim.termo.records;

import br.com.azalim.termo.states.MatchState;

public record WordGrid(Word[] words, MatchState state) {

    public String asString() {

        StringBuilder stringBuilder = new StringBuilder();

        for (Word word : words) {
            for (Letter letter : word.letters()) {

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
