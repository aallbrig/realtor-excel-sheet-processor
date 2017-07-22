package com.andrewallbright.app.models;

import com.andrewallbright.app.interfaces.ValidRow;
import com.andrewallbright.app.options.RowOption;
import com.andrewallbright.app.rules.Rules;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

public class ValidOverflowCommentRow implements ValidRow {
    private static DataFormatter formatter;
    static { formatter = new DataFormatter(); }
    private Row internalRowRef;
    public Optional<String> agentOverflowComment;

    public ValidOverflowCommentRow(Row r) {
        internalRowRef = r;
        agentOverflowComment = extractAgentOverflowComment(r);
    }

    static public Boolean isValid(Row rowRef) {
        return Rules.isWithValidOverflowComment(rowRef);
    }

    public static ValidTargetAgentRow findCorrespondingAgentRow(ValidOverflowCommentRow targetRow, Set<Row> rowList, int indexAwayFromTargetRow) {
        Instant methodStartTime = new Date().toInstant();
        int targetRowNum = targetRow.getRow().map(Row::getRowNum).orElseThrow(NullPointerException::new);
        int lowerBounds = targetRowNum - indexAwayFromTargetRow;
        Optional<ValidTargetAgentRow> correspondingAgentRow = rowList
            .parallelStream()
            .filter(tempRow -> tempRow.getRowNum() > lowerBounds && tempRow.getRowNum() < targetRowNum)
            .filter(ValidTargetAgentRow::isValid)
            .map(ValidTargetAgentRow::new)
            .reduce((a, b) -> b);
        Duration methodDuration = Duration.between(methodStartTime, new Date().toInstant());
        System.out.println("Single findCorrespondingAgentRow timing: " + methodDuration.getSeconds() + "s");
        return correspondingAgentRow.orElseGet(
            () -> findCorrespondingAgentRow(targetRow, rowList, indexAwayFromTargetRow + 1)
        );
    }

    private Optional<String> extractAgentOverflowComment(Row rowRef) {
        String dataExtraction = formatter.formatCellValue(rowRef.getCell(RowOption.COLUMN_B.value()));
        return !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

    public Optional<Row> getRow() {
        return Optional.of(internalRowRef);
    }

    public Optional<Integer> getRowNum() {
        return Optional.of(internalRowRef.getRowNum());
    }


}
