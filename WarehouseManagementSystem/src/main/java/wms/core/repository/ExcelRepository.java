package wms.core.repository;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelRepository {

    private static final String DB = "warehouse_db.xlsx";
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ExcelRepository INSTANCE = new ExcelRepository();

    public static ExcelRepository get() {
        return INSTANCE;
    }

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
            sheet(wb, "STOCK", "UPC", "SKU", "BatchDate", "Qty", "Location");
            sheet(wb, "LOG", "Time", "Action", "UPC", "SKU", "Qty", "From", "To");
            Sheet loc = sheet(wb, "LOCATIONS", "Location");

            for (String d : List.of(
                    "PA-01-01-01-01",
                    "PA-01-02-01-01",
                    "RA-01-01-01-01",
                    "RA-01-01-01-02"
            )) {
                loc.createRow(loc.getLastRowNum() + 1)
                        .createCell(0)
                        .setCellValue(d);
            }

            try (FileOutputStream fos = new FileOutputStream(DB)) {
                wb.write(fos);
            }
        }
    }

    private Sheet sheet(Workbook wb, String name, String... headers) {
        Sheet s = wb.createSheet(name);
        Row h = s.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            h.createCell(i).setCellValue(headers[i]);
        }
        return s;
    }

    private static String s(Cell c) {
        return c == null ? "" : c.toString().trim();
    }

    private static int i(Cell c) {
        return c == null ? 0 : (int) c.getNumericCellValue();
    }

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<>();
        execute(wb -> {
            Sheet s = wb.getSheet("LOCATIONS");
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                String code = s(s.getRow(i).getCell(0));
                if (!code.isBlank()) {
                    list.add(Location.fromBarcode(code));
                }
            }
        });
        return list;
    }

    public boolean isLocationUsed(Location loc) {
        final boolean[] used = {false};

        execute(wb -> {
            Sheet s = wb.getSheet("STOCK");
            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) continue;

                String locCode = s(r.getCell(4));
                int qty = i(r.getCell(3));

                if (loc.toBarcode().equals(locCode) && qty > 0) {
                    used[0] = true;
                    break;
                }
            }
        });

        return used[0];
    }

    public void updateLocation(Location oldLoc, Location newLoc) {
        execute(wb -> {

            Sheet locSheet = wb.getSheet("LOCATIONS");
            for (int i = 1; i <= locSheet.getLastRowNum(); i++) {
                Row r = locSheet.getRow(i);
                if (r == null) continue;

                if (oldLoc.toBarcode().equals(s(r.getCell(0)))) {
                    r.getCell(0).setCellValue(newLoc.toBarcode());
                }
            }

            Sheet stockSheet = wb.getSheet("STOCK");
            for (int i = 1; i <= stockSheet.getLastRowNum(); i++) {
                Row r = stockSheet.getRow(i);
                if (r == null) continue;

                if (oldLoc.toBarcode().equals(s(r.getCell(4)))) {
                    r.getCell(4).setCellValue(newLoc.toBarcode());
                }
            }
        });
    }

    public void deleteLocation(Location loc) {
        execute(wb -> {
            Sheet s = wb.getSheet("LOCATIONS");

            for (int i = 1; i <= s.getLastRowNum(); i++) {
                Row r = s.getRow(i);
                if (r == null) continue;

                if (loc.toBarcode().equals(s(r.getCell(0)))) {
                    s.removeRow(r);
                    break;
                }
            }
        });
    }

    public void saveStockBatch(StockBatch b) {
        execute(wb -> {
            Sheet s = wb.getSheet("STOCK");
            Row r = s.createRow(s.getLastRowNum() + 1);
            r.createCell(0).setCellValue(b.upc);
            r.createCell(1).setCellValue(b.sku);
            r.createCell(2).setCellValue(b.batchDate.toString());
            r.createCell(3).setCellValue(b.quantity);
            r.createCell(4).setCellValue(b.location.toBarcode());
        });
    }

    public List<StockBatch> getAllStock() {
        List<StockBatch> list = new ArrayList<>();

        execute(wb -> {
            Sheet s = wb.getSheet("STOCK");

            for (int row = 1; row <= s.getLastRowNum(); row++) {
                try {
                    Row r = s.getRow(row);
                    if (r == null) continue;

                    String upc = s(r.getCell(0));
                    String sku = s(r.getCell(1));
                    String dateStr = s(r.getCell(2));
                    int qty = i(r.getCell(3));
                    String locStr = s(r.getCell(4));

                    if (upc.isBlank() || sku.isBlank() || qty <= 0 || dateStr.isBlank() || locStr.isBlank())
                        continue;

                    LocalDate batchDate = LocalDate.parse(dateStr);
                    Location loc = Location.fromBarcode(locStr);

                    list.add(new StockBatch(
                            upc,
                            sku,
                            batchDate,
                            qty,
                            loc
                    ));
                } catch (Exception ignore) {
                }
            }
        });

        return list;
    }

    public void log(
            String action,
            String upc,
            String sku,
            int qty,
            Location from,
            Location to
    ) {
        execute(wb -> {
            Sheet s = wb.getSheet("LOG");
            Row r = s.createRow(s.getLastRowNum() + 1);
            r.createCell(0).setCellValue(now());
            r.createCell(1).setCellValue(action);
            r.createCell(2).setCellValue(upc);
            r.createCell(3).setCellValue(sku);
            r.createCell(4).setCellValue(qty);
            r.createCell(5).setCellValue(from != null ? from.toBarcode() : "");
            r.createCell(6).setCellValue(to != null ? to.toBarcode() : "");
        });
    }
}
