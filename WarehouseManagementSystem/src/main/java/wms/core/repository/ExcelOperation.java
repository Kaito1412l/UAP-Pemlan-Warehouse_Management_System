package wms.core.repository;

import org.apache.poi.ss.usermodel.Workbook;

@FunctionalInterface
public interface ExcelOperation {
    void run(Workbook workbook);
}
