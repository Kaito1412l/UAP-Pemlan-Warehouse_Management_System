package wms.core.repository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelRepository {

    private static final String DB = "warehouse_db.xlsx";
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ExcelRepository INSTANCE = new ExcelRepository();
    public static ExcelRepository get() { return INSTANCE; }

    private ExcelRepository() {}

    public synchronized void execute(ExcelOperation op) {
        try {
            initIfNeeded();
            try (FileInputStream fis = new FileInputStream(DB);
                 Workbook wb = new XSSFWorkbook(fis)) {

                op.run(wb);

                try (FileOutputStream fos = new FileOutputStream(DB)) {
                    wb.write(fos);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void initIfNeeded() throws IOException {
        if (Files.exists(Path.of(DB))) return;

        try (Workbook wb = new XSSFWorkbook()) {
            sheet(wb, "STOCK", "UPC","SKU","BatchDate","Qty","Location");
            sheet(wb, "LOG", "Time","Action","UPC","SKU","Qty","From","To");
            Sheet loc = sheet(wb, "LOCATIONS", "Location");
            for (String d : List.of(
                    "PA-01-01-01-01","PA-01-02-01-01",
                    "RA-01-01-01-01","RA-01-01-01-02")) {
                loc.createRow(loc.getLastRowNum()+1).createCell(0).setCellValue(d);
            }
            try (FileOutputStream fos = new FileOutputStream(DB)) {
                wb.write(fos);
            }
        }
    }

    private Sheet sheet(Workbook wb, String name, String... headers) {
        Sheet s = wb.createSheet(name);
        Row h = s.createRow(0);
        for (int i=0;i<headers.length;i++) h.createCell(i).setCellValue(headers[i]);
        return s;
    }

    public static String s(Cell c) {
        return c==null?"":c.toString().trim();
    }

    public static int i(Cell c) {
        return c==null?0:(int)c.getNumericCellValue();
    }

    public static String now() {
        return LocalDateTime.now().format(TS);
    }

    public List<String> locations() {
        List<String> l = new ArrayList<>();
        execute(wb -> {
            Sheet s = wb.getSheet("LOCATIONS");
            for (int r=1;r<=s.getLastRowNum();r++)
                l.add(s(s.getRow(r).getCell(0)));
        });
        return l;
    }
}
