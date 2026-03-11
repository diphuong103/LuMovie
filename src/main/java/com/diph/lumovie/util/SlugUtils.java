package com.diph.lumovie.util;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
public class SlugUtils {
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    public static String toSlug(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_LATIN.matcher(WHITESPACE.matcher(n).replaceAll("-")).replaceAll("").toLowerCase(Locale.ENGLISH);
    }
}
