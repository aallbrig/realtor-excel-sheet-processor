package com.andrewallbright.app.options;

import org.junit.Test;

import static org.junit.Assert.*;

public class RowOptionTest {
    @Test
    public void verifiesAllValues() {
        System.out.println("Row Options:");
        for (RowOption rowOption : RowOption.values()) {
            System.out.println(rowOption);
        }
        assertEquals(0, RowOption.COLUMN_A.value());
        assertEquals(1, RowOption.COLUMN_B.value());
        assertEquals(2, RowOption.COLUMN_C.value());
        assertEquals(4, RowOption.COLUMN_H.value());
        assertEquals(5, RowOption.COLUMN_I.value());
        assertEquals(5, RowOption.values().length);
    }
}