package com.google.api.channelsales.grcn;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/*
 * README: The CSV downloaded from WildCat is in Little-endian UTF-16 encoding
 * Need to convert it to UTF8 encoding: iconv -f UTF-16 -t UTF-8 $sourcefile > $TMPDIR$filename
 */

public class TradeYepDataProcessor {
  private static final Logger LOGGER = Logger.getLogger(TradeYepDataProcessor.class);

  private static String THIS_QUARTER = "15Q2";
  private static int MONTHS_IN_QUARTER = 3;
  private static DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##");

  private static String inputFilePath  = "/Users/zhuoc/Downloads/Wildcat_Stats_country_vertical_grcnaccounts_utf8.csv";
  private static String outputFilePath = "/Users/zhuoc/Downloads/Trade_Yep_Result_" + THIS_QUARTER + ".csv";
  private static String[] outputHeaders = {"Quarter", "Country", "Vertical", "Revenue", "Advertiser Count", "ARPA"};
  
  private static void setLogLevel() {

    Level logLevel = Level.DEBUG;
    
    ConsoleAppender console = new ConsoleAppender(); // create appender
    String pattern = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout(new PatternLayout(pattern));
    console.activateOptions();
    console.setThreshold(logLevel);
    Logger.getLogger("com.google.api.channelsales.grcn").addAppender(console);

    FileAppender fa = new FileAppender();
    fa.setName("FileLogger");
    fa.setFile("trade-yep-data-processor.log");
    fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
    fa.setThreshold(logLevel);
    fa.setAppend(true);
    fa.activateOptions();
    Logger.getLogger("com.google.api.channelsales.grcn").addAppender(fa);
  }

  public void processCsvFile() throws IOException {
    
    CSVReader csvReader = new CSVReader(new FileReader(inputFilePath), '\t');
    LOGGER.info("Processing CSV reports from wildcat...");
    
    LOGGER.info("Processing CSV header...");
    TradeYepDataCenter dataCenter = new TradeYepDataCenter(csvReader.readNext());
    
    LOGGER.info("Processing CSV data...");
    String[] line;
    while ((line = csvReader.readNext()) != null) {
      dataCenter.processCsvLine(line);
    }
    
    LOGGER.info("Closing CSV file...");
    csvReader.close();
    
    LOGGER.info("Producing results...");
    outputResult(dataCenter.produceResult());
  }
  
  public void outputResult(Map<String, Map<String, AccountsCostPerVertical>> data) throws IOException {
    LOGGER.info("Outputing results in CSV file: " + outputFilePath);
    CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath, false));
    
    writer.writeNext(outputHeaders);
    
    // L1 key: geo, L2 key: vertical
    for (Map.Entry<String, Map<String, AccountsCostPerVertical>> entry: data.entrySet()) {
      String geo = entry.getKey();
      
      for (Map.Entry<String, AccountsCostPerVertical> subentry : entry.getValue().entrySet()) {
        String vertical = subentry.getKey();
        AccountsCostPerVertical value = subentry.getValue();
        int numAccounts = value.getNumAccounts();
        double totalCost = value.getTotalCost();
        double arpa = totalCost / numAccounts / MONTHS_IN_QUARTER;

        String[] line = {THIS_QUARTER, geo, vertical, Double.toString(totalCost), Integer.toString(numAccounts), DOUBLE_FORMAT.format(arpa)};
        writer.writeNext(line);
      }
    }
    
    writer.close();
    LOGGER.info("All done!");
  }
  
  public static void main(String[] args) throws Exception {
    setLogLevel();
    
    TradeYepDataProcessor processor = new TradeYepDataProcessor();
    processor.processCsvFile();
  }
}
