package wms.core.service;

import wms.core.repository.ExcelRepository;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.util.*;

public class OutboundService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void pick(String upc, String loc, int qty) {
        if (!loc.startsWith("R")) throw new RuntimeException("Outbound dari R-area");

        repo.execute(wb -> {
            Sheet s = wb.getSheet("STOCK");
            Sheet log = wb.getSheet("LOG");

            List<Row> rows = new ArrayList<>();
            for (int i=1;i<=s.getLastRowNum();i++) {
                Row r = s.getRow(i);
                if (upc.equals(ExcelRepository.s(r.getCell(0))) &&
                        loc.equals(ExcelRepository.s(r.getCell(4))) &&
                        ExcelRepository.i(r.getCell(3))>0)
                    rows.add(r);
            }

            rows.sort(Comparator.comparing(r ->
                    LocalDate.parse(ExcelRepository.s(r.getCell(2)))));

            int remain = qty;
            for (Row r : rows) {
                if (remain<=0) break;
                int cur = ExcelRepository.i(r.getCell(3));
                int take = Math.min(cur, remain);
                r.getCell(3).setCellValue(cur-take);
                remain -= take;

                Row lg = log.createRow(log.getLastRowNum()+1);
                lg.createCell(0).setCellValue(ExcelRepository.now());
                lg.createCell(1).setCellValue("OUTBOUND");
                lg.createCell(2).setCellValue(upc);
                lg.createCell(3).setCellValue(ExcelRepository.s(r.getCell(1)));
                lg.createCell(4).setCellValue(take);
                lg.createCell(6).setCellValue(loc);
            }

            if (remain>0) throw new RuntimeException("Stok kurang");
        });
    }
}
