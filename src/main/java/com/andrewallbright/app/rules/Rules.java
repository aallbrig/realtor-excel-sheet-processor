package com.andrewallbright.app.rules;

import com.andrewallbright.app.options.RowOption;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.Set;

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

    public static Boolean isWithValidAgentTarget(Row targetRow) {
        String agentIdPattern = "\\d+";
        String agentNamePattern = "^([a-zA-Z]{2,}\\s[a-zA-z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?)";
        String maybeAgentId = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String maybeAgentName = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        return
            maybeAgentId.matches(agentIdPattern)
            && maybeAgentName.matches(agentNamePattern)
            && !maybeAgentId.trim().isEmpty()
            && !maybeAgentName.trim().isEmpty();
    }

    public static Boolean isWithValidAgentTarget(Set<Row> targetRows) {
        return targetRows.stream()
            .map(Rules::isWithValidAgentTarget)
            .reduce(true, (r1, r2) -> r1 && r2);
    }

    public static Boolean isWithOverflowCommentRow(Row targetRow) {
        String colAValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String colBValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        String colGValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_G.value()));
//        Sentence maybeSentence = new Sentence(colBValue);
//        SemanticGraph x = maybeSentence.dependencyGraph();
        return !isWithValidAgentTarget(targetRow)
            && !colBValue.trim().isEmpty()
            && colAValue.trim().isEmpty()
            && colGValue.trim().isEmpty()
            && formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value())).trim().isEmpty();
    }

    public static Boolean isWithOverflowCommentRow(Set<Row> targetRows) {
        return targetRows.stream()
            .map(Rules::isWithOverflowCommentRow)
            .reduce(true, (r1, r2) -> r1 && r2);
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
        Boolean ruleCheck = colAVal.contains(desiredColAVal)
            && colBVal.contains(desiredColBVal)
            && colCVal.contains(desiredColCVal)
            && colGVal.contains(desiredColGVal)
            && colHVal.contains(desiredColHVal)
            && colIVal.contains(desiredColIVal);
        return ruleCheck;
    }

    public static Boolean isWithValidHeadersRow(Set<Row> targetRows) {
        return targetRows.stream()
            .map(Rules::isWithValidHeadersRow)
            .reduce(true, (r1, r2) -> r1 && r2);
    }

    public static Boolean isIgnoredRow(Row targetRow) {
        return !isWithValidAgentTarget(targetRow) && !isWithValidHeadersRow(targetRow) && !isWithOverflowCommentRow(targetRow);
    }
}
