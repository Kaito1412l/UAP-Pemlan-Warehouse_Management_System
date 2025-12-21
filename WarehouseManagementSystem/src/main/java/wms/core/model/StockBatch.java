package wms.core.model;

import java.time.LocalDate;

public final class StockBatch {

    public final String upc;
    public final String sku;
    public final LocalDate batchDate;
    public final int quantity;
    public final Location location;

    public StockBatch(
            String upc,
            String sku,
            LocalDate batchDate,
            int quantity,
            Location location
    ) {
        if (upc == null || upc.isBlank())
            throw new IllegalArgumentException("UPC wajib diisi");

        if (sku == null || sku.isBlank())
            throw new IllegalArgumentException("SKU wajib diisi");

        if (batchDate == null)
            throw new IllegalArgumentException("Batch date wajib diisi");

        if (quantity < 0)
            throw new IllegalArgumentException("Quantity tidak boleh negatif");

        if (location == null)
            throw new IllegalArgumentException("Location wajib diisi");

        this.upc = upc;
        this.sku = sku;
        this.batchDate = batchDate;
        this.quantity = quantity;
        this.location = location;
    }
}
