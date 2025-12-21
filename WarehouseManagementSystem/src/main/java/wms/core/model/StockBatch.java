package wms.core.model;

import java.time.LocalDate;

public class StockBatch {
    public final String upc;
    public final String sku;
    public final LocalDate batchDate;
    public final int quantity;
    public final String location;

    public StockBatch(String upc, String sku, LocalDate batchDate, int quantity, String location) {
        this.upc = upc;
        this.sku = sku;
        this.batchDate = batchDate;
        this.quantity = quantity;
        this.location = location;
    }
}
