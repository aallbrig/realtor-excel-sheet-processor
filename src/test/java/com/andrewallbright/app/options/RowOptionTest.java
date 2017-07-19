package com.andrewallbright.app.options;

import org.junit.Test;

import static org.junit.Assert.*;

public class RowOptionTest {
    @Test
    public void verifiesAllValues() {
        assertEquals(0, RowOption.COLUMN_A.value());
        assertEquals(1, RowOption.COLUMN_B.value());
        assertEquals(2, RowOption.COLUMN_C.value());
        assertEquals(6, RowOption.COLUMN_G.value());
        assertEquals(7, RowOption.COLUMN_H.value());
        assertEquals(8, RowOption.COLUMN_I.value());
        assertEquals(6, RowOption.values().length);
    }
}