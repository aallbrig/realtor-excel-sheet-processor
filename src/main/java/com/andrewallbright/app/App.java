package com.andrewallbright.app;

import com.andrewallbright.app.models.ValidTargetAgentRow;
import com.andrewallbright.app.models.ValidOverflowCommentRow;
import org.apache.commons.cli.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        options.addOption("i", true, "Input file");
        options.addOption("o", true, "Output file");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("i")) {
            Instant wbOpenStartTime = new Date().toInstant();
            try (Workbook wb = WorkbookFactory.create(new File(cmd.getOptionValue("i")))) {
                Instant wbOpenComplete = new Date().toInstant();
                Duration workbookOpenDuration = Duration.between(wbOpenStartTime, wbOpenComplete);
                Sheet sheet = wb.getSheetAt(SheetOption.PRIMARY_SHEET.value());
                int totalRowsProcessed = sheet.getPhysicalNumberOfRows();

                // TODO: Find out better way to write below code.  Ideally, I could use (Row row : sheet1)
                // generate a streamable collection.
                List<Row> rowList = new ArrayList<>();
                for (Row row : sheet) rowList.add(row);

                Set<Row> headerRow = rowList.parallelStream()
                    .filter(Rules::isWithValidHeadersRow)
                    .collect(Collectors.toSet());

                Set<String> headerRowColVals = headerRow.stream()
                    .flatMap(r -> {
                        HashSet<String> tmp = new HashSet<>();
                        for (Cell c : r) tmp.add(formatter.formatCellValue(c));
                        return tmp.stream();
                    })
                    .collect(Collectors.toSet());

                List<Map.Entry<ValidTargetAgentRow, Optional<Set<ValidOverflowCommentRow>>>> targetRowsToOverflowRows = IntStream.range(0, rowList.size())
                    .parallel()
                    .filter(index -> ValidTargetAgentRow.isValid(rowList.get(index)))
                    .mapToObj(index -> {
                        Row r = rowList.get(index);
                        HashMap<ValidTargetAgentRow, Optional<Set<ValidOverflowCommentRow>>> foo = new HashMap<>();
                        int acceptableRowDistance = 120;
                        Instant start = new Date().toInstant();
                        Optional<Set<ValidOverflowCommentRow>> correspondingOverflowCommentRows =
                            ValidTargetAgentRow.findCorrespondingOverflowCommentRows(
                                rowList.stream().skip(index + 1).limit(acceptableRowDistance).collect(Collectors.toList()),
                                1
                            );
                        // TODO: Find better perf system than this (look @ awesome java for libs)
                        Duration end = Duration.between(start, new Date().toInstant());
                        foo.put(new ValidTargetAgentRow(r), correspondingOverflowCommentRows);
                        return foo;
                    })
                    .flatMap((map) -> map.entrySet().stream())
                    .collect(Collectors.toList());

                // Set values of overflow comments of OverflowCommentRow to agent comments of TargetAgentRow
                // Bonus: count how many agent comments from target rows have quotation marks present in the cell (REGEX)
                targetRowsToOverflowRows.stream()
                    .forEach(entry -> {
                        ValidTargetAgentRow targetRow = entry.getKey();
                        Optional<Set<ValidOverflowCommentRow>> overflowCommentRows = entry.getValue();
                        String combinedComments =
                            "\"" +
                            (targetRow.agentComment.map(s -> s + (overflowCommentRows.isPresent() ? "\n" : "")).orElse(""))
                            + (overflowCommentRows.map(validOverflowCommentRows -> validOverflowCommentRows.stream()
                                // ensure overflow comments are sorted by row number
                                .sorted(Comparator.comparingInt(r -> r.internalRowRef.getRowNum()))
                                .map(r -> r.agentOverflowComment.get())
                                .collect(Collectors.joining("\n"))
                                ).orElse(""))
                            + "\"";
                        // TODO: Rewrite below section to deal with nulls more elegantly.
                        if (targetRow.internalRowRef != null) {
                            Cell cell = (
                                targetRow
                                    .internalRowRef
                                    .getCell(ValidTargetAgentRow.agentCommentCell) != null ?
                                targetRow
                                    .internalRowRef
                                    .getCell(ValidTargetAgentRow.agentCommentCell)
                                : targetRow
                                        .internalRowRef.createCell(ValidTargetAgentRow.agentCommentCell)
                            );
                            cell.setCellValue(combinedComments);
                        } else {
                            System.out.println("null row ref for " + targetRow.getRowNum());
                        }
                    });


                // NOTE: Below seems to be a basis for a test assertion
                // TODO: Work on App tests in JUnit
//                Set<ValidOverflowCommentRow> targetRowsWithPresentComments = agentRowsWithPresentOverflowComments.stream()
//                    .map(Map.Entry::getValue)
//                    .map(Optional::get)
//                    .flatMap(Collection::stream)
//                    .collect(Collectors.toSet());
//                Set<Optional<String>> overflowRowsFromProcessedTargetData = rowList
//                    .parallelStream()
//                    .filter(ValidOverflowCommentRow::isValid)
//                    .map(ValidOverflowCommentRow::new)
//                    .map(r -> r.agentOverflowComment)
//                    .collect(Collectors.toSet());
//                System.out.println("overflowRowsFromProcessedTargetData.size() == overflowRowsFromCompleteRowSet.size() ?");
//                System.out.println(targetRowsWithPresentComments.size() == overflowRowsFromProcessedTargetData.size());
                // Above should be false, as written, because there are invalid agent rows that have overflow comments.
                // end "basis for test assertion" section

                if (cmd.hasOption("o")) {
                    Instant fileWriteStartTime = new Date().toInstant();
                    // TODO: Figure out why cmd.getOptionValue("o") expression is returning null
                    Optional<String> outputFileName = Optional.of(cmd.getOptionValue("o"));
                    System.out.println(outputFileName);
                    File outputFile = new File(outputFileName.orElse("../Processed Data.xlsx"));
                    outputFile.createNewFile();
                    wb.write(new FileOutputStream(outputFile));
                    Instant fileWriteEndTime = new Date().toInstant();
                    System.out.println("File Write Time: " + App.humanReadableSeconds(
                        Duration.between(fileWriteStartTime, fileWriteEndTime).getSeconds()
                    ));
                }

                System.out.println("Workbook Open Time: " + App.humanReadableSeconds(workbookOpenDuration.getSeconds()));
                System.out.println("Header values: ");
                headerRowColVals.forEach((headerVal) -> System.out.print(headerVal + ", "));
                System.out.println();
                System.out.println("total number of rows processed: " + totalRowsProcessed);
                System.out.println("# of rows probably deleted (given the number of the last row): " + (sheet.getLastRowNum() - totalRowsProcessed));
                // TODO: Do something with these variables (delete: 1, test assertions: 0, output stats: 1)
//                Set<Row> ignoredRows = rowList.stream()
//                    .filter(Rules::isIgnoredRow)
//                    .collect(Collectors.toSet());
//                System.out.println("# of ignored rows: " + ignoredRows.size());

                Set<String> uniqueAgentNames = rowList.stream()
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())))
                    .collect(Collectors.toSet());

                Set<String> uniqueAgentIds = rowList.stream()
                    .filter(r -> !Rules.isWithValidHeadersRow(r))
                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())))
                    .collect(Collectors.toSet());
                System.out.println("# of rows that matched rule set and were modified: " + targetRowsToOverflowRows.size());
                System.out.println("# of rows that match rules set and have overflow comments that were added to Column I: "
                    + targetRowsToOverflowRows.parallelStream()
                        .filter(entry -> entry.getValue().isPresent())
                        .collect(Collectors.toSet()).size());
                System.out.println("# of unique agent names: " + uniqueAgentNames.size());
                System.out.println("# of unique agent ids: " + uniqueAgentIds.size());

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
