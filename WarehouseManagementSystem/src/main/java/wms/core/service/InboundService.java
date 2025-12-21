package wms.core.service;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import wms.core.repository.ExcelRepository;

import java.time.LocalDate;

public class InboundService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void receive(
            String upc,
            String manufacturer,
            String category,
            int qty,
            Location location
    ) {
        if (!location.isInboundArea()) {
            throw new RuntimeException("Inbound hanya ke P-area");
        }

        String sku = manufacturer + "-" + category + "-" + LocalDate.now();

        StockBatch batch = new StockBatch(
                upc,
                sku,
                LocalDate.now(),
                qty,
                location
        );

        repo.saveStockBatch(batch);
        repo.log("INBOUND", upc, sku, qty, null, location);
    }
}
