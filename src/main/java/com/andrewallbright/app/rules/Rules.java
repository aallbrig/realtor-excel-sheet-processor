package com.andrewallbright.app.rules;

import com.andrewallbright.app.options.RowOption;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class Rules {
    private static DataFormatter formatter;
    static { formatter = new DataFormatter(); }

    public static Boolean isRowWithDate(Row targetRow) {
        Cell maybeDate = targetRow.getCell(RowOption.COLUMN_C.value());
        return maybeDate.getCellTypeEnum() == CellType.NUMERIC && HSSFDateUtil.isCellDateFormatted(maybeDate);
    }

    public static boolean isFirstRow(Row targetRow) {
        return targetRow.getRowNum() == 0;
    }

    public static Boolean isValidTargetRow(Row targetRow) {
        String maybeAgentId = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String maybeAgentName = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        return !maybeAgentId.trim().isEmpty() && !maybeAgentName.trim().isEmpty();
    }

    public static Boolean isRowWithOverflowComments(Row targetRow) {
        return !isValidTargetRow(targetRow)
            && !formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value())).trim().isEmpty()
            && formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value())).trim().isEmpty();
    }

    public static Boolean isRowWithCorrectHeaders(Row targetRow) {
        String desiredColAVal = "BL Agent ID";
        String desiredColBVal = "Contact_ID";
        String desiredColCVal = "ContactFirst";
        String desiredColGVal = "Author_Type";
        String desiredColHVal = "Author_AgentName";
        String desiredColIVal = "Contact_Note";
        String colAVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String colBVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        String colCVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_C.value()));
        String colGVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_G.value()));
        String colHVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        String colIVal = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_I.value()));
        Boolean ruleCheck = colAVal.contains(desiredColAVal)
                && colBVal.contains(desiredColBVal)
                && colCVal.contains(desiredColCVal)
                && colGVal.contains(desiredColGVal)
                && colHVal.contains(desiredColHVal)
                && colIVal.contains(desiredColIVal);
        if (ruleCheck) {
            System.out.println("T");
        }
        return ruleCheck;
    }

    public static Boolean isIgnoredRow(Row targetRow) {
        // TODO: Implement
        return false;
    }
}
