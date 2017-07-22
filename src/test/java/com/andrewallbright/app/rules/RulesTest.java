package com.andrewallbright.app.rules;

import com.andrewallbright.app.options.RowOption;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class RulesTest {
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

    }
    @Test
    public void isFirstRow() throws Exception {
        Row validRow = sheet.createRow(++currentRowIndex);
        Row invalidRow = sheet.createRow(++currentRowIndex);
        assertEquals(true, Rules.isFirstRow(validRow));
        assertEquals(false, Rules.isFirstRow(invalidRow));
    }

    @Test
    public void isRowWithCorrectHeaders() throws Exception {
        String desiredColAVal = "BL Agent ID";
        String desiredColBVal = "Contact_ID";
        String desiredColCVal = "ContactFirst";
        String desiredColGVal = "Author_Type";
        String desiredColHVal = "Author_AgentName";
        String desiredColIVal = "Contact_Note";
        Row validRow = sheet.createRow(++currentRowIndex);
        validRow.createCell(RowOption.COLUMN_A.value()).setCellValue(desiredColAVal);
        validRow.createCell(RowOption.COLUMN_B.value()).setCellValue(desiredColBVal);
        validRow.createCell(RowOption.COLUMN_C.value()).setCellValue(desiredColCVal);
        validRow.createCell(RowOption.COLUMN_G.value()).setCellValue(desiredColGVal);
        validRow.createCell(RowOption.COLUMN_H.value()).setCellValue(desiredColHVal);
        validRow.createCell(RowOption.COLUMN_I.value()).setCellValue(desiredColIVal);
        Row invalidRow = sheet.createRow(++currentRowIndex);
        assertEquals(true, Rules.isWithValidHeadersRow(validRow));
        assertEquals(false, Rules.isWithValidHeadersRow(invalidRow));
    }

    @Test
    public void isValidTargetRow() throws Exception {
        Row validRow = sheet.createRow(++currentRowIndex);
        validRow.createCell(RowOption.COLUMN_A.value()).setCellValue(123456789);
        validRow.createCell(RowOption.COLUMN_H.value()).setCellValue("Tom Bob");

        HashSet<Row> validRows = new HashSet<>();
        validRows.add(validRow);

        Row validOverflowComment = sheet.createRow(++currentRowIndex);
        validOverflowComment.createCell(RowOption.COLUMN_B.value()).setCellValue("This is a valid agent comment");

        HashSet<Row> validAgentRowAndOverflowCommentRow = new HashSet<>();
        validAgentRowAndOverflowCommentRow.add(validRow);
        validAgentRowAndOverflowCommentRow.add(validOverflowComment);

        Row invalidRow = sheet.createRow(++currentRowIndex);
        invalidRow.createCell(RowOption.COLUMN_A.value()).setCellValue("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        invalidRow.createCell(RowOption.COLUMN_H.value()).setCellValue(123456789);

        Row invalidAgentNameOnly = sheet.createRow(++currentRowIndex);
        invalidAgentNameOnly.createCell(RowOption.COLUMN_A.value()).setCellValue(123456789);
        invalidAgentNameOnly.createCell(RowOption.COLUMN_H.value()).setCellValue(123456789);

        Row invalidAgentIdOnly = sheet.createRow(++currentRowIndex);
        invalidAgentIdOnly.createCell(RowOption.COLUMN_A.value()).setCellValue("BL Agent ID");
        invalidAgentIdOnly.createCell(RowOption.COLUMN_H.value()).setCellValue("Tom Bob");

        HashSet<Row> invalidAgentRowAndOverflowCommentRow = new HashSet<>();
        validAgentRowAndOverflowCommentRow.add(validOverflowComment);
        validAgentRowAndOverflowCommentRow.add(validRow);

        assertEquals(true, Rules.isWithValidAgentTarget(validRow));
        assertEquals(true, Rules.isWithValidAgentTarget(validRows));
        assertEquals(false, Rules.isWithValidAgentTarget(invalidRow));
        assertEquals(false, Rules.isWithValidAgentTarget(invalidAgentRowAndOverflowCommentRow));
        assertEquals(false, Rules.isWithValidAgentTarget(invalidAgentNameOnly));
        assertEquals(false, Rules.isWithValidAgentTarget(invalidAgentIdOnly));
    }

//    @Test
//    public void isWithValidOverflowComment() throws Exception {
//        Workbook wb = new HSSFWorkbook();
//        Sheet sheet = wb.createSheet("test sheet");
//        Row validRow = sheet.createRow(1);
//        Row invalidRow = sheet.createRow(2);
//        assertEquals(true, Rules.isWithValidOverflowComment(validRow));
//        assertEquals(false, Rules.isWithValidOverflowComment(invalidRow));
//    }
//
//
//    @Test
//    public void isIgnoredRow() throws Exception {
//        Workbook wb = new HSSFWorkbook();
//        Sheet sheet = wb.createSheet("test sheet");
//        Row validRow = sheet.createRow(1);
//        Row invalidRow = sheet.createRow(2);
//        assertEquals(true, Rules.isIgnoredRow(validRow));
//        assertEquals(false, Rules.isIgnoredRow(invalidRow));
//    }
}