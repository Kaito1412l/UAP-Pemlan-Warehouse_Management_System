package wms.core.service;

import wms.core.repository.ExcelRepository;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.util.*;

public class InventoryService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void relocate(String upc, String sku, String from, String to, int qty) {
        repo.execute(wb -> {
            Sheet s = wb.getSheet("STOCK");
            Sheet log = wb.getSheet("LOG");

            List<Row> rows = new ArrayList<>();
            for (int i=1;i<=s.getLastRowNum();i++) {
                Row r = s.getRow(i);
                if (upc.equals(ExcelRepository.s(r.getCell(0))) &&
                        sku.equals(ExcelRepository.s(r.getCell(1))) &&
                        from.equals(ExcelRepository.s(r.getCell(4))) &&
                        ExcelRepository.i(r.getCell(3))>0)
                    rows.add(r);
            }

            rows.sort(Comparator.comparing(r ->
                    LocalDate.parse(ExcelRepository.s(r.getCell(2)))));

            int remain = qty;
            for (Row r : rows) {
                if (remain<=0) break;
                int cur = ExcelRepository.i(r.getCell(3));
                int move = Math.min(cur, remain);
                r.getCell(3).setCellValue(cur-move);
                remain -= move;

                Row nr = s.createRow(s.getLastRowNum()+1);
                nr.createCell(0).setCellValue(upc);
                nr.createCell(1).setCellValue(sku);
                nr.createCell(2).setCellValue(r.getCell(2).toString());
                nr.createCell(3).setCellValue(move);
                nr.createCell(4).setCellValue(to);

                Row lg = log.createRow(log.getLastRowNum()+1);
                lg.createCell(0).setCellValue(ExcelRepository.now());
                lg.createCell(1).setCellValue("RELOCATE");
                lg.createCell(2).setCellValue(upc);
                lg.createCell(3).setCellValue(sku);
                lg.createCell(4).setCellValue(move);
                lg.createCell(6).setCellValue(from);
                lg.createCell(7).setCellValue(to);
            }

            if (remain>0) throw new RuntimeException("Stok tidak cukup");
        });
    }
}
