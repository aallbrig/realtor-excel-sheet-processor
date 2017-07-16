package com.andrewallbright.app;

import org.apache.commons.cli.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.andrewallbright.app.options.*;
import com.andrewallbright.app.rules.Rules;


public class App {
    private static DataFormatter formatter;
    static { formatter = new DataFormatter(); }

    public static String humanReadableSeconds(long seconds) {
        return String.format("%02d hours %02d minutes %02d seconds", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }

    public static void main(String[] args) throws IOException, InvalidFormatException, ParseException {
        System.out.println("Program Start");
        Instant startTime = new Date().toInstant();
        Duration programDuration;

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        options.addOption("i", true, "Input file");
        options.addOption("o", false, "Output file");
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("i")) {
            Instant wbOpenStartTime = new Date().toInstant();
            try (Workbook wb = WorkbookFactory.create(new File(cmd.getOptionValue("i")))) {
                Instant wbOpenComplete = new Date().toInstant();
                Duration workbookOpenDuration = Duration.between(wbOpenStartTime, wbOpenComplete);
                Sheet sheet1 = wb.getSheetAt(SheetOption.PRIMARY_SHEET.value());
                int totalRowsProcessed = sheet1.getPhysicalNumberOfRows();

                // TODO: Find out better way to write below code.  Ideally, I could use (Row row : sheet1)
                // generate a streamable collection.
                HashSet<Row> rowList = new HashSet<>();
                for (Row row : sheet1) {
                    rowList.add(row);
                }

                Set<Row> headerRow = rowList.stream()
                    .filter(Rules::isRowWithCorrectHeaders)
                    .collect(Collectors.toSet());

                Set<String> headerRowColVals = headerRow.stream()
                    .flatMap(r -> {
                        HashSet<String> tmp = new HashSet<>();
                        for (Cell c : r) tmp.add(formatter.formatCellValue(c));
                        return tmp.stream();
                    })
                    .collect(Collectors.toSet());

                Set<Row> ignoredRows = rowList.stream()
                    .filter(Rules::isIgnoredRow)
                    .collect(Collectors.toSet());

                Set<String> uniqueAgentNames = rowList.stream()
                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())))
                    .collect(Collectors.toSet());

                Set<String> uniqueAgentIds = rowList.stream()
                        .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())))
                        .collect(Collectors.toSet());

                // TODO: Do something with these variables
                Set<Row> firstRow = rowList.stream()
                        .filter(Rules::isFirstRow)
                        .collect(Collectors.toSet());
                Set<Row> rowsWithOverflowComments = rowList.stream()
                        .filter(Rules::isRowWithOverflowComments)
                        .collect(Collectors.toSet());
                Set<Row> validTargetRows = rowList.stream()
                        .filter(Rules::isValidTargetRow)
                        .collect(Collectors.toSet());

                System.out.println("Workbook Open Time: " + App.humanReadableSeconds(workbookOpenDuration.getSeconds()));
                headerRowColVals.forEach((headerVal) -> System.out.println("Header Value: " + headerVal));
                System.out.println("total number of rows processed: " + totalRowsProcessed);
                System.out.println("# of Unique Agent Names: " + uniqueAgentNames.size());
                System.out.println("# of Unique Agent Names: " + uniqueAgentIds.size());
                System.out.println("# of ignored rows: " + ignoredRows.size());
                System.out.println("rows that match rule set: " + (totalRowsProcessed - ignoredRows.size()));

                System.out.println("Program End");
                programDuration = Duration.between(startTime, new Date().toInstant());
                System.out.println("Program Duration: " + App.humanReadableSeconds(programDuration.getSeconds()));
            } catch (NullPointerException e) {
                System.out.println("\n\n\n:( NPE while reading file\n\n");

                System.out.println("\n\n\nProgram End");
                programDuration = Duration.between(startTime, new Date().toInstant());
                System.out.println("Program Duration: " + App.humanReadableSeconds(programDuration.getSeconds()));
                throw(e);
            }
        }
    }
}
