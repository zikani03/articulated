package me.zikani.labs.articulated.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AmountTest {
    @Test
    public void testRegex() {
        assertTrue(Amount.KWACHA_REGEX_2.matcher("MWK 123 million").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("MWK 123 trillion").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("MK 123 trillion").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("K 123 trillion").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("K 123 billion").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("K 123 million").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("MWK 123 billion").find());
        assertTrue(Amount.KWACHA_REGEX_2.matcher("MWK 123.23 billion").find());
    }
}