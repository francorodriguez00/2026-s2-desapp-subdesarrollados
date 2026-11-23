package com.furbo.service.matching;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String normalizeName(String name) {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        
        normalized = normalized.replace("ø", "o").replace("đ", "d").replace("ł", "l")
                               .replace("ß", "ss").replace("æ", "ae").replace("œ", "oe")
                               .replace("ð", "d").replace("þ", "th");
        
        normalized = normalized.replaceAll("[^a-z0-9]", " ");
        return normalized.replaceAll("\\s+", " ").trim();
    }

    public static List<String> tokens(String name) {
        String normalized = normalizeName(name);
        return Arrays.stream(normalized.split("\\s+"))
                     .filter(t -> !t.isEmpty())
                     .collect(Collectors.toList());
    }

    public static String normalizeTeam(String team) {
        String normalized = normalizeName(team);
        return Arrays.stream(normalized.split("\\s+"))
                     .filter(t -> !List.of("fc", "cf", "afc", "sc", "ac", "cd", "ud").contains(t))
                     .collect(Collectors.joining(" "));
    }

    public static String mapPosition(String pos, boolean isWhoScored) {
        if (pos == null) return null;
        if (isWhoScored) {
            return switch (pos) {
                case "Goalkeeper" -> "GK";
                case "Defender" -> "DF";
                case "Midfielder" -> "MF";
                case "Forward" -> "FW";
                default -> null;
            };
        } else {
            return switch (pos) {
                case "Goalkeeper" -> "GK";
                case "Defence" -> "DF";
                case "Midfield" -> "MF";
                case "Offence" -> "FW";
                default -> null;
            };
        }
    }

    public static double jaroWinkler(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int len1 = s1.length(), len2 = s2.length();
        if (len1 == 0 || len2 == 0) return 0.0;
        int matchDistance = Math.max(len1, len2) / 2 - 1;
        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];
        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true; s2Matches[j] = true; matches++; break;
            }
        }
        if (matches == 0) return 0.0;
        int transpositions = 0, k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }
        double jaro = ((double) matches / len1 + (double) matches / len2 + (double) (matches - transpositions / 2) / matches) / 3.0;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(len1, len2)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return jaro + 0.1 * prefix * (1.0 - jaro);
    }
}
