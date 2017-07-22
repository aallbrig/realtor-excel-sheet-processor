package com.andrewallbright.app;

import com.andrewallbright.app.models.ValidTargetAgentRow;
import com.andrewallbright.app.models.ValidOverflowCommentRow;
import org.apache.commons.cli.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
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
        options.addOption("o", false, "Output file");
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

                List<Map.Entry<ValidTargetAgentRow, Optional<Set<ValidOverflowCommentRow>>>> targetRowsToOverflowRows = IntStream.range(0, rowList.size())
                    .parallel()
                    .filter(index -> {
                        Row r = rowList.get(index);
                        return ValidTargetAgentRow.isValid(r);
                    })
                    .mapToObj(index -> {
                        Row r = rowList.get(index);
                        HashMap<ValidTargetAgentRow, Optional<Set<ValidOverflowCommentRow>>> foo = new HashMap<>();
                        int acceptableRowDistance = 20;
                        Instant start = new Date().toInstant();
                        Optional<Set<ValidOverflowCommentRow>> correspondingOverflowCommentRows =
                            ValidTargetAgentRow.findCorrespondingOverflowCommentRows(
                                rowList.stream().skip(index + 1).limit(acceptableRowDistance).collect(Collectors.toList()),
                                1
                            );
                        Duration end = Duration.between(start, new Date().toInstant());
                        foo.put(new ValidTargetAgentRow(r), correspondingOverflowCommentRows);
                        return foo;
                    })
                    .flatMap((map) -> map.entrySet().stream())
                    .collect(Collectors.toList());

                List<Map.Entry<ValidTargetAgentRow, Optional<Set<ValidOverflowCommentRow>>>> agentRowsWithOverflowComments = targetRowsToOverflowRows.stream()
                    .filter(entry -> entry.getValue().isPresent())
                    .collect(Collectors.toList());

                // note: This seems to be a basis for a test assertion
                Set<ValidOverflowCommentRow> overflowRowsFromAgentRows = agentRowsWithOverflowComments.stream()
                    .map(Map.Entry::getValue).map(Optional::get).flatMap(Collection::stream).collect(Collectors.toSet());
                Set<Optional<String>> overflowRowsFromCompleteRowSet = rowList
                    .parallelStream()
                    .filter(ValidOverflowCommentRow::isValid)
                    .map(ValidOverflowCommentRow::new)
                    .map(r -> r.agentOverflowComment)
                    .collect(Collectors.toSet());
                System.out.println("overflowRowsFromAgentRows.size() == overflowRowsFromCompleteRowSet.size() ?");
                System.out.println(overflowRowsFromAgentRows.size() == overflowRowsFromCompleteRowSet.size());
                // Above should be false, as written, because there are invalid agent rows that have overflow comments.

                System.out.println("Workbook Open Time: " + App.humanReadableSeconds(workbookOpenDuration.getSeconds()));
                headerRowColVals.forEach((headerVal) -> System.out.println("Header Value: " + headerVal));
                System.out.println("total number of rows processed: " + totalRowsProcessed);
                // TODO: Do something with these variables
//                Set<Row> ignoredRows = rowList.stream()
//                    .filter(Rules::isIgnoredRow)
//                    .collect(Collectors.toSet());
//
//                Set<String> uniqueAgentNames = rowList.stream()
//                    .filter(r -> !Rules.isWithValidHeadersRow(r))
//                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_H.value())))
//                    .collect(Collectors.toSet());
//
//                Set<String> uniqueAgentIds = rowList.stream()
//                    .filter(r -> !Rules.isWithValidHeadersRow(r))
//                    .map(r -> formatter.formatCellValue(r.getCell(RowOption.COLUMN_A.value())))
//                    .collect(Collectors.toSet());
//                System.out.println("# of Unique Agent Names: " + uniqueAgentNames.size());
//                System.out.println("# of Unique Agent Ids: " + uniqueAgentIds.size());
//                System.out.println("# of ignored rows: " + ignoredRows.size());
//                System.out.println("# of rows that match rule set: " + (totalRowsProcessed - ignoredRows.size()));

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
