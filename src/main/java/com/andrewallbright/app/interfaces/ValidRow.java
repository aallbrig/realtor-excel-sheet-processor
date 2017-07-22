package com.andrewallbright.app.interfaces;

import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;

public interface ValidRow {
    static Boolean isValid(Row refRow) {
        return false;
    };
    Optional<Row> getRow();
    Optional<Integer> getRowNum();
}
