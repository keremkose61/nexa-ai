package com.nexa.common;

import com.nexa.common.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testFormat() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 21, 10, 30, 0);
        String formatted = DateUtils.format(now);
        assertEquals("2026-11-21 10:30:00", formatted);
    }

    @Test
    void testFormatNull() {
        assertNull(DateUtils.format(null));
    }

    @Test
    void testIsExpired() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        LocalDateTime future = LocalDateTime.now().plusMinutes(10);

        assertTrue(DateUtils.isExpired(past));
        assertFalse(DateUtils.isExpired(future));
    }
}
