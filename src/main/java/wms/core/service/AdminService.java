package wms.core.service;

import wms.core.repository.ExcelRepository;
import org.apache.poi.ss.usermodel.*;

public class AdminService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void addLocation(String loc) {
        repo.execute(wb -> {
            Sheet s = wb.getSheet("LOCATIONS");
            for (int i=1;i<=s.getLastRowNum();i++)
                if (loc.equals(ExcelRepository.s(s.getRow(i).getCell(0))))
                    throw new RuntimeException("Lokasi sudah ada");
            s.createRow(s.getLastRowNum()+1).createCell(0).setCellValue(loc);
        });
    }
}
