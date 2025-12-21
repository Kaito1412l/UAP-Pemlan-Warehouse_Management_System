package wms.core.service;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import wms.core.repository.ExcelRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void relocate(
            String upc,
            String sku,
            Location from,
            Location to,
            int qty
    ) {
        List<StockBatch> available = repo.getAllStock().stream()
                .filter(b -> b.upc.equals(upc))
                .filter(b -> b.sku.equals(sku))
                .filter(b -> b.location.equals(from))
                .filter(b -> b.quantity > 0)
                .sorted(Comparator.comparing(b -> b.batchDate))
                .collect(Collectors.toList());

        int remain = qty;

        for (StockBatch b : available) {
            if (remain <= 0) break;

            int move = Math.min(b.quantity, remain);
            remain -= move;

            StockBatch moved = new StockBatch(
                    b.upc,
                    b.sku,
                    b.batchDate,
                    move,
                    to
            );

            repo.saveStockBatch(moved);
            repo.log("RELOCATE", b.upc, b.sku, move, from, to);
        }

        if (remain > 0) {
            throw new RuntimeException("Stok tidak cukup untuk relokasi");
        }
    }
}
