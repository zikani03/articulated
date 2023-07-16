package me.zikani.labs.articulated.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeywordOccurencesTest {

    @Test
    public void testOccurencesCount() {
        Article article = new Article();
        article.setBody("donate xxxxx donate xxxx donate xxxx donate xxxx donate xxxx donate");

        assertEquals(6, article.countOccurencesOf("donate"));
    }
}
