package com.furbo.service.matching;

import com.furbo.model.Player;
import java.util.*;

public class NameSimilarity {

    public record Result(String rule, double score) {}

    public static Result evaluate(String wsName, String wsFirstName, String wsLastName, Player player) {
        String pName = player.getName();
        String pTeam = player.getTeam();
        String pPos = player.getPosition();

        List<String> wsNameVariants = new ArrayList<>();
        if (wsName != null && !wsName.isBlank()) {
            wsNameVariants.add(NameNormalizer.normalizeName(wsName));
        }
        if (wsFirstName != null && !wsFirstName.isBlank() && wsLastName != null && !wsLastName.isBlank()) {
            wsNameVariants.add(NameNormalizer.normalizeName(wsFirstName + " " + wsLastName));
        }

        String normPlayerName = NameNormalizer.normalizeName(pName);

        Result best = null;
        for (String wsn : wsNameVariants) {
            Result r = compareNormalized(wsn, normPlayerName);
            if (r != null) {
                if (best == null || r.score() > best.score()) {
                    best = r;
                }
            }
        }

        if (best == null) {
            return null;
        }

        double finalScore = best.score();
        if (isPositionCompatible(player.getPosition(), player.getPosition())) { // placeholder position compatibility check
            finalScore = Math.min(1.0, finalScore + 0.03);
        }

        return new Result(best.rule(), finalScore);
    }

    public static Result compareNormalized(String wsNorm, String playerNorm) {
        if (wsNorm.equals(playerNorm)) {
            return new Result("CONTAINED", 0.93);
        }

        List<String> wsTokens = Arrays.asList(wsNorm.split("\\s+"));
        List<String> pTokens = Arrays.asList(playerNorm.split("\\s+"));

        // CONTAINED
        if (isContained(wsTokens, pTokens) || isContained(pTokens, wsTokens)) {
            return new Result("CONTAINED", 0.93);
        }

        // ABBREVIATION
        if (isAbbreviation(wsTokens, pTokens)) {
            return new Result("ABBREVIATION", 0.92);
        }

        // TYPO
        if (isTypo(wsTokens, pTokens) || wsNorm.replace(" ", "").equals(playerNorm.replace(" ", ""))) {
            return new Result("TYPO", 0.90);
        }

        // SURNAME_ONLY
        if (!wsTokens.isEmpty() && !pTokens.isEmpty()) {
            String wsSurname = wsTokens.get(wsTokens.size() - 1);
            String pSurname = pTokens.get(pTokens.size() - 1);
            if (wsSurname.equals(pSurname) && wsSurname.length() >= 3) {
                return new Result("SURNAME_ONLY", 0.60);
            }
        }

        return null;
    }

    private static boolean isContained(List<String> shorter, List<String> longer) {
        if (shorter.size() >= longer.size()) return false;
        if (shorter.size() == 1 && shorter.get(0).length() < 4) return false;

        List<String> remaining = new ArrayList<>(longer);
        for (String t : shorter) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).equals(t)) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean isAbbreviation(List<String> t1, List<String> t2) {
        if (t1.size() == 1 && t2.size() == 1) {
            String s1 = t1.get(0);
            String s2 = t2.get(0);
            return (s2.startsWith(s1) || s1.startsWith(s2)) && Math.min(s1.length(), s2.length()) >= 4;
        }

        if (t1.size() != t2.size()) return false;
        boolean abbrFound = false;
        String surname1 = t1.get(t1.size() - 1);
        String surname2 = t2.get(t2.size() - 1);
        if (!surname1.equals(surname2)) return false;

        for (int i = 0; i < t1.size(); i++) {
            String s1 = t1.get(i);
            String s2 = t2.get(i);
            if (s1.equals(s2)) continue;

            if (s2.startsWith(s1) && s1.length() >= 3) {
                abbrFound = true;
            } else if (s1.startsWith(s2) && s2.length() >= 3) {
                abbrFound = true;
            } else {
                return false;
            }
        }
        return abbrFound;
    }

    private static boolean isTypo(List<String> t1, List<String> t2) {
        if (t1.size() != t2.size()) return false;
        boolean exactMatchAtLeastOne = false;
        for (int i = 0; i < t1.size(); i++) {
            String s1 = t1.get(i);
            String s2 = t2.get(i);
            if (s1.equals(s2)) {
                exactMatchAtLeastOne = true;
                continue;
            }
            int threshold = s1.length() >= 8 || s2.length() >= 8 ? 2 : 1;
            if (levenshteinDistance(s1, s2) <= threshold) {
                // allowed
            } else {
                return false;
            }
        }
        return exactMatchAtLeastOne;
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    public static boolean isPositionCompatible(String wsPos, String pPos) {
        if (wsPos == null || pPos == null) return true;
        String w = wsPos.toLowerCase();
        String p = pPos.toLowerCase();
        if (w.contains("goalk") && p.contains("goalk")) return true;
        if (w.contains("defend") && p.contains("defen")) return true;
        if (w.contains("midfield") && p.contains("midfield")) return true;
        if ((w.contains("forward") || w.contains("offence") || w.contains("striker")) && (p.contains("offence") || p.contains("forward"))) return true;
        return false;
    }
}
