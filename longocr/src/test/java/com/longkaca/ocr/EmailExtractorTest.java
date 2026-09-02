package com.longkaca.ocr;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class EmailExtractorTest {
    @Test public void extractsNormalEmail() {
        assertEquals("long.kaca+01@gmail.com", EmailExtractor.firstEmail("Mail: long.kaca+01@gmail.com end"));
    }

    @Test public void joinsOcrSpacesAroundSeparators() {
        assertEquals("long_01@gmail.com", EmailExtractor.firstEmail("long_01 @ gmail . com"));
    }

    @Test public void rejectsIncompleteAddress() {
        assertEquals("", EmailExtractor.firstEmail("long@gmail"));
    }
}
