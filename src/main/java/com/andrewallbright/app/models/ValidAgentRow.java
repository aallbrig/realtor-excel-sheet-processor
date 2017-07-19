package com.andrewallbright.app.models;

import com.andrewallbright.app.interfaces.ValidRow;
import com.andrewallbright.app.options.RowOption;
import com.andrewallbright.app.rules.Rules;
import jdk.nashorn.internal.runtime.options.Option;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;

public class ValidAgentRow implements ValidRow {
    private static DataFormatter formatter;
    static { formatter = new DataFormatter(); }
    private Row rowRef;

    public ValidAgentRow(Row r) {
        rowRef = r;
    }

    public Boolean isValid() {
        return Rules.isWithValidAgentTarget(rowRef);
    }

    public Optional<String> getAgentId() {
        String dataExtraction = formatter.formatCellValue(rowRef.getCell(RowOption.COLUMN_A.value()));
        return this.isValid() && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

    public Optional<String> getAgentName() {
        String dataExtraction = formatter.formatCellValue(rowRef.getCell(RowOption.COLUMN_H.value()));
        return this.isValid() && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
    }

//    public Optional<String> getAgentComments() {
//        String dataExtraction = formatter.formatCellValue(rowRef.getCell(RowOption.COLUMN_I.value()));
//        return this.isValid() && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
//    }
//
//    public Optional<String> getAgentOverflowComments() {
//        String dataExtraction = formatter.formatCellValue(rowRef.getCell(RowOption.COLUMN_B.value()));
//        return this.isValid() && !dataExtraction.isEmpty() ? Optional.of(dataExtraction) : Optional.empty();
//    }
}
