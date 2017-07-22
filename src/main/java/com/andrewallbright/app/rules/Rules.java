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

    public static Boolean isWithInvalidAgentTarget(Row targetRow) {
        String maybeContactId = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        return contactIdValue.matcher(maybeContactId).matches() && !isWithValidAgentTarget(targetRow);
    }

    public static Boolean isWithValidOverflowComment(Row targetRow) {
        Instant methodStartTime = new Date().toInstant();
        String colAValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_A.value()));
        String colBValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_B.value()));
        String colGValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_G.value()));
        String colHValue = formatter.formatCellValue(targetRow.getCell(RowOption.COLUMN_H.value()));
        // TODO: Maybe do some sentiment analysis for further confidence?
        // Sentence maybeSentence = new Sentence(colBValue);
        // SemanticGraph x = maybeSentence.dependencyGraph();
        Boolean ruleCheck = !(contactIdValue.matcher(colBValue).matches())
                && !colBValue.trim().isEmpty()
                && colAValue.trim().isEmpty()
                && colGValue.trim().isEmpty()
                && colHValue.trim().isEmpty();
        Duration methodDuration = Duration.between(methodStartTime, new Date().toInstant());
        return ruleCheck;
    }

    public static Boolean isWithValidOverflowComment(Set<Row> targetRows) {
        return targetRows.stream()
            .map(Rules::isWithValidOverflowComment)
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
        return !isWithValidAgentTarget(targetRow) && !isWithValidHeadersRow(targetRow) && !isWithValidOverflowComment(targetRow);
    }
}
