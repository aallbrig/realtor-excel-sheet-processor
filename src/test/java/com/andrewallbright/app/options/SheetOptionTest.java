package com.andrewallbright.app.options;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SheetOptionTest {
    @Test
    public void checksPrimarySheetIndex() {
        System.out.println(0 == SheetOption.PRIMARY_SHEET.value());
        assertEquals(0, SheetOption.PRIMARY_SHEET.value());
    }
}