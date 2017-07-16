package com.andrewallbright.app.options;

public enum RowOption {
    COLUMN_A(0),
    COLUMN_B(1),
    COLUMN_C(2),
    COLUMN_G(6),
    COLUMN_H(7),
    COLUMN_I(8);

    private int rowIndex;

    RowOption(int rowIndex) { this.rowIndex = rowIndex; }

    public int value() { return rowIndex; }

}