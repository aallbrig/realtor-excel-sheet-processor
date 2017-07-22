package com.andrewallbright.app.models;

import com.andrewallbright.app.interfaces.ValidRow;
import com.andrewallbright.app.rules.Rules;
import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;
import java.util.regex.Pattern;

public class InvalidTargetAgentRow implements ValidRow {
    private static Pattern contactIdValue;
    private Row internalRowRef;
    {
        contactIdValue = Pattern.compile("^(((\\w){3,}+(-)?){4,})");
    }
    public InvalidTargetAgentRow(Row r) {
        internalRowRef = r;
    }

    public static boolean isValid(Row targetRow) {
        return Rules.isWithInvalidAgentTarget(targetRow);
    }

    public Optional<Row> getRow() {
        return Optional.of(internalRowRef);
    }

    public Optional<Integer> getRowNum() {
        return Optional.of(internalRowRef.getRowNum());
    }
}
