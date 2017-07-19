package com.andrewallbright.app.models;

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidAgentRowTest {
    Workbook wb;
    Sheet sheet;
    Integer currentRowIndex;
    static String testSheetName;
    {
        testSheetName = "test sheet";
    }
    @Before
    public void setUp() throws Exception {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet(testSheetName);
        currentRowIndex = 0;
    }

    @After
    public void tearDown() throws Exception {
        currentRowIndex = 0;
    }
    @Test
    public void canGetAgentName() throws Exception {

    }
}