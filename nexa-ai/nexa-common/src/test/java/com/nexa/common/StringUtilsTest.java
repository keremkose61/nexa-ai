package com.nexa.common;

import com.nexa.common.util.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("nexa"));
    }

    @Test
    void testTruncate() {
        assertNull(StringUtils.truncate(null, 10));
        assertEquals("short", StringUtils.truncate("short", 10));
        assertEquals("hello w...", StringUtils.truncate("hello world from nexa", 10));
    }

    @Test
    void testSanitize() {
        assertNull(StringUtils.sanitize(null));
        assertEquals("clean", StringUtils.sanitize("clean<>'\"&"));
    }

    @Test
    void testEstimateTokenCount() {
        assertEquals(0, StringUtils.estimateTokenCount(null));
        assertEquals(1, StringUtils.estimateTokenCount("Hi"));
        assertEquals(3, StringUtils.estimateTokenCount("Hello World!"));
    }
}
