package com.andrewallbright.app.options;

public enum SheetOption {
    PRIMARY_SHEET(0);

    private int sheetIndex;

    SheetOption(int sheetIndex) { this.sheetIndex = sheetIndex; }

    public int value() { return sheetIndex; }

}