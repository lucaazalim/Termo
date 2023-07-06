package br.com.azalim.wordle;

import java.text.Normalizer;

public class Util {

    public static String normalizeString(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

}
