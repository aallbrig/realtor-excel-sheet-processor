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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


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
        options.addOption("i", true, "Input file");
        options.addOption("o", false, "Output file");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("i")) {
            Instant wbOpenStartTime = new Date().toInstant();
            try (Workbook wb = WorkbookFactory.create(new File(cmd.getOptionValue("i")))) {
                Instant wbOpenComplete = new Date().toInstant();
                Duration workbookOpenDuration = Duration.between(wbOpenStartTime, wbOpenComplete);
                Sheet sheet = wb.getSheetAt(SheetOption.PRIMARY_SHEET.value());

                System.out.println("Row Breaks" + Arrays.toString(sheet.getRowBreaks());
                int totalRowsProcessed = sheet.getPhysicalNumberOfRows();

                // TODO: Find out better way to write below code.  Ideally, I could use (Row row : sheet1)
                // generate a streamable collection.
                HashSet<Row> rowList = new HashSet<>();
                for (Row row : sheet) rowList.add(row);

                Set<Row> headerRow = rowList.stream()
                    .filter(Rules::isWithValidHeadersRow)
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
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())))
                    .collect(Collectors.toSet());

                Set<String> uniqueAgentIds = rowList.stream()
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())))
                    .collect(Collectors.toSet());

                // TODO: Do something with these variables
                Set<Row> validTargetRows = rowList.stream()
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .filter(Rules::isWithValidAgentTarget)
                    .collect(Collectors.toSet());

                Set<Row> rowsWithOverflowComments = rowList.stream()
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .filter(Rules::isWithOverflowCommentRow)
                    .collect(Collectors.toSet());

//                Map<String, List<Row>> x = rowList.stream()
//                    .collect(groupingBy(
//                        r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())),
//                        mapping(r -> r, toList())
//                    ));

                Map<String, List<String>> y = rowList.stream()
                    .filter(r -> Rules.isWithValidAgentTarget(r) || Rules.isWithOverflowCommentRow(r))
                    .sorted(Comparator.comparing(Row::getRowNum))
                    .collect(groupingBy(
                        r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())),
                        mapping(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())), toList())
                    ));

                Map<String, List<String>> y = rowList.stream()
                        .filter(r -> Rules.isWithValidAgentTarget(r) || Rules.isWithOverflowCommentRow(r))
                        .sorted(Comparator.comparing(Row::getRowNum))
                        .collect(groupingBy(
                                r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())),
                                mapping(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())), toList())
                        ));
//                Map<String, List<Row>> z = rowList.stream()
//                    .filter(r -> Rules.isWithValidAgentTarget(r) || Rules.isWithOverflowCommentRow(r));

                System.out.println("Workbook Open Time: " + App.humanReadableSeconds(workbookOpenDuration.getSeconds()));
                headerRowColVals.forEach((headerVal) -> System.out.println("Header Value: " + headerVal));
                System.out.println("total number of rows processed: " + totalRowsProcessed);
                System.out.println("# of Unique Agent Names: " + uniqueAgentNames.size());
                System.out.println("# of Unique Agent Ids: " + uniqueAgentIds.size());
                System.out.println("# of ignored rows: " + ignoredRows.size());
                System.out.println("# of rows that match rule set: " + (totalRowsProcessed - ignoredRows.size()));

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
