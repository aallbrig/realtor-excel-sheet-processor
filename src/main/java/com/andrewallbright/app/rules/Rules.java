package com.andrewallbright.app.rules;

import com.andrewallbright.app.options.RowOption;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

public class Rules {
    private static DataFormatter formatter;
    private static Pattern contactIdValue;
    static {
        formatter = new DataFormatter();
        contactIdValue = Pattern.compile("^(((\\w){3,}+(-)?){4,})");
    }

    public static boolean isFirstRow(Row targetRow) {
        return targetRow.getRowNum() == 0;
    }

    public static Boolean isWithValidAgentTarget(Row targetRow) {
        String agentIdPattern = "\\d+";
        String maybeAgentId = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String maybeAgentName = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        return
            maybeAgentId.matches(agentIdPattern)
            && maybeAgentName.length() > 3
            && !maybeAgentId.trim().isEmpty()
            && !maybeAgentName.trim().isEmpty();
    }

    public static Boolean isWithValidAgentTarget(Set<Row> targetRows) {
        return targetRows.stream()
            .map(Rules::isWithValidAgentTarget)
            .reduce(true, (r1, r2) -> r1 && r2);
    }

    public static Boolean isWithInvalidAgentTarget(Row targetRow) {
        String maybeContactId = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        return contactIdValue.matcher(maybeContactId).matches() && !isWithValidAgentTarget(targetRow);
    }

    public static Boolean isWithValidOverflowComment(Row targetRow) {
        String colAValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String colBValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        String colGValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_G.value()));
        String colHValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        // TODO: Maybe do some sentiment analysis for further confidence?
        return !(contactIdValue.matcher(colBValue).matches())
                && !colBValue.trim().isEmpty()
                && colAValue.trim().isEmpty()
                && colGValue.trim().isEmpty()
                && colHValue.trim().isEmpty();
    }

    public static Boolean isWithValidHeadersRow(Row targetRow) {
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
        return colAVal.contains(desiredColAVal)
            && colBVal.contains(desiredColBVal)
            && colCVal.contains(desiredColCVal)
            && colGVal.contains(desiredColGVal)
            && colHVal.contains(desiredColHVal)
            && colIVal.contains(desiredColIVal);
    }
}
