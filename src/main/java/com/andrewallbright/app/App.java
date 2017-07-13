package com.andrewallbright.app;

import org.apache.commons.cli.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class App
{

    public static String humanReadableSeconds (long seconds) {
        return String.format("%02d hours %02d minutes %02d seconds", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }
    public static void main( String[] args ) throws IOException, InvalidFormatException, ParseException {
        System.out.println("Program Start");
        Instant startTime = new Date().toInstant();

        DataFormatter formatter = new DataFormatter();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        Set<String> uniqueAgentNames = new HashSet<>();
        Set<String> uniqueAgentIds = new HashSet<>();
        // Keep track of unique rows that are _ignored_ and clear this when you see a row that _matches_
        // TODO: Make into hash map
        Set<String> uniqueRowsInReadState = new HashSet<>();
        Duration programDuration;
        int ignoredRows = 0;
        int totalRowsProcessed = 0;
        boolean readState = false;


        options.addOption("i", true, "Input file");
        options.addOption("o", false, "Output file");
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("i")) {
            Instant wbOpenStartTime = new Date().toInstant();
            Workbook wb = WorkbookFactory.create(new File(cmd.getOptionValue("i")));
            Instant wbOpenComplete = new Date().toInstant();
            long workbookOpenTimeInSeconds = Duration.between(wbOpenStartTime, wbOpenComplete).getSeconds();
            Sheet sheet1 = wb.getSheetAt(0);
            // TODO: Reconsider how "total rows processed" is computed.
            totalRowsProcessed = sheet1.getPhysicalNumberOfRows();

            for (Row row : sheet1) {
                Cell columnA = row.getCell(0);  // agent id
                Cell columnH = row.getCell(4);  // agent name
                Cell columnI = row.getCell(5);  // agent comments
                Cell columnB = row.getCell(1);  // maybe overflow comments
                String colAVal = formatter.formatCellValue(columnA);
                String colHVal = formatter.formatCellValue(columnH);
                String colIVal = formatter.formatCellValue(columnI);
                String colBVal = formatter.formatCellValue(columnB);
                System.out.println("row " + row.getRowNum());
                System.out.println(
                    "    "
                    + (!colAVal.contentEquals("") ? ", col A - " + colAVal : "")
                    + (!colHVal.contentEquals("") ? ", col H - " + colHVal : "")
                    + (!colIVal.contentEquals("") ? ", col I - " + colIVal : "")
                    + (!colBVal.contentEquals("") ? ", col B - " + colBVal : "")
                );
                if (colAVal == "" || colHVal == "") {
                    ignoredRows += 1;
                    System.out.println("...ignoring row");
                    // if readState
                    if (readState)
                        uniqueAgentNames.add(colHVal);
                      // read in specific column values into memory
                } else if (!(colAVal == "" && colHVal == "")) {
                    if (!uniqueAgentNames.contains(colHVal))
                        uniqueAgentNames.add(colHVal);
                    if (!uniqueAgentIds.contains(colAVal))
                        uniqueAgentIds.add(colAVal);
                    // stop read state when matching a valid rule set (this elif)
                    if (readState)
                        readState = false;
                }
            }

            System.out.println("Workbook Open Time: " + App.humanReadableSeconds(workbookOpenTimeInSeconds));
        }

        System.out.println("# of Unique Agent Names: " + uniqueAgentNames.size());
        System.out.println("# of Unique Agent Names: " + uniqueAgentIds.size());
        System.out.println("total number of rows processed: " + totalRowsProcessed);
        System.out.println("ignored rows: " + ignoredRows);
        System.out.println("rows that match rule set: " + (totalRowsProcessed - ignoredRows));

        System.out.println("Program End");
        programDuration = Duration.between(startTime, new Date().toInstant());
        System.out.println("Program Duration: " + App.humanReadableSeconds(programDuration.getSeconds()));
    }
}
