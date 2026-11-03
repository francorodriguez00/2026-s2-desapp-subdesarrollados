package com.furbo.service.matching;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NameNormalizerTest {

    @Test
    void testNormalizeName() {
        assertEquals("ronald araujo", NameNormalizer.normalizeName("Ronald Araújo"));
        assertEquals("mateo kovacic", NameNormalizer.normalizeName("Mateo Kovačić"));
        assertEquals("josko gvardiol", NameNormalizer.normalizeName("Joško Gvardiol"));
        assertEquals("jeremy jacquet", NameNormalizer.normalizeName("Jérémy Jacquet"));
        assertEquals("wataru endo", NameNormalizer.normalizeName("Wataru Endō"));
        assertEquals("o reilly", NameNormalizer.normalizeName("O'Reilly"));
        assertEquals("sonni lambie", NameNormalizer.normalizeName("Sonni-Lambie"));
        assertEquals("o", NameNormalizer.normalizeName("ø"));
        assertEquals("l", NameNormalizer.normalizeName("ł"));
        assertEquals("ss", NameNormalizer.normalizeName("ß"));
        assertEquals("", NameNormalizer.normalizeName(null));
    }
}
