package wms.core.service;

import wms.core.repository.ExcelRepository;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;

public class InboundService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void receive(String upc, String manu, String cat, int qty, String loc) {
        if (!loc.startsWith("P")) throw new RuntimeException("Inbound ke P-area");

        repo.execute(wb -> {
            Sheet st = wb.getSheet("STOCK");
            Sheet log = wb.getSheet("LOG");
            String sku = manu+"-"+cat+"-"+LocalDate.now();

            Row r = st.createRow(st.getLastRowNum()+1);
            r.createCell(0).setCellValue(upc);
            r.createCell(1).setCellValue(sku);
            r.createCell(2).setCellValue(LocalDate.now().toString());
            r.createCell(3).setCellValue(qty);
            r.createCell(4).setCellValue(loc);

            Row l = log.createRow(log.getLastRowNum()+1);
            l.createCell(0).setCellValue(ExcelRepository.now());
            l.createCell(1).setCellValue("INBOUND");
            l.createCell(2).setCellValue(upc);
            l.createCell(3).setCellValue(sku);
            l.createCell(4).setCellValue(qty);
            l.createCell(6).setCellValue(loc);
        });
    }
}
