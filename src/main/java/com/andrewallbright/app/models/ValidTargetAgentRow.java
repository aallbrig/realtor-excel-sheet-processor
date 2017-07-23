package com.andrewallbright.app.models;

import com.andrewallbright.app.interfaces.ValidRow;
import com.andrewallbright.app.options.RowOption;
import com.andrewallbright.app.rules.Rules;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidTargetAgentRow implements ValidRow {
    private static DataFormatter formatter = new DataFormatter();
    public static int agentCommentCell = RowOption.COLUMN_I.value();
    public static int agentIdCell = RowOption.COLUMN_A.value();
    public static int agentNameCell = RowOption.COLUMN_H.value();
    public Row internalRowRef;
    public Optional<String> agentName;
    public Optional<String> agentId;
    public Optional<String> agentComment;


    public ValidTargetAgentRow(Row r) {
        internalRowRef = r;
        agentId = extractAgentId(r);
        agentName = extractAgentName(r);
        agentComment = extractAgentComment(r);
    }

    static public boolean isValid(Row rowRef) {
        return Rules.isWithValidAgentTarget(rowRef);
    }

    private Optional<String> extractAgentComment(Row r) {
        String dataExtraction = formatter.formatCellValue(r.getCell(agentCommentCell));
        return isValid(r) && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

    public Optional<String> extractAgentId(Row r) {
        String dataExtraction = formatter.formatCellValue(r.getCell(agentIdCell));
        return isValid(r) && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

    public Optional<String> extractAgentName(Row r) {
        String dataExtraction = formatter.formatCellValue(r.getCell(agentNameCell));
        return isValid(r) && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

    public static Optional<Set<ValidOverflowCommentRow>> findCorrespondingOverflowCommentRows(List<Row> rowList, int indexAwayFromTargetRow) {
        // NOTE: If rowList is too large, that will significantly increase time of operation.
        Set<Row> rowRange = rowList.parallelStream().limit(indexAwayFromTargetRow).collect(Collectors.toSet());
        long validTargetRows = rowRange.parallelStream().filter(ValidTargetAgentRow::isValid).count();
        long invalidTargetRows = rowRange.parallelStream().filter(InvalidTargetAgentRow::isValid).count();
        // Below code assumes that there only exists "invalid target rows", "valid target rows" and "overflow comment";
        // if there exists any type except "overflow comment" type then stop increasing row range
        boolean stopCollecting = validTargetRows > 0 || invalidTargetRows > 0;
        if (stopCollecting) {
            Set<ValidOverflowCommentRow> overflowCommentRows =
                rowRange.stream()
                .skip(0).limit(indexAwayFromTargetRow)
                .filter(ValidOverflowCommentRow::isValid)
                .map(ValidOverflowCommentRow::new)
                .collect(Collectors.toSet());
            return overflowCommentRows.size() > 0 ? Optional.of(overflowCommentRows) : Optional.empty();
        } if (indexAwayFromTargetRow > rowList.size()) {
            return Optional.empty();
        }  else {
            return findCorrespondingOverflowCommentRows(rowList, indexAwayFromTargetRow + 1);
        }
    }

    public Optional<Row> getRow() {
        return Optional.of(internalRowRef);
    }

    public Optional<Integer> getRowNum() {
        return Optional.of(internalRowRef.getRowNum());
    }
}
