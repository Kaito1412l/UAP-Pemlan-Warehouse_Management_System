package wms.core.service;

import wms.core.model.Location;
import wms.core.model.StockBatch;
import wms.core.repository.ExcelRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OutboundService {

    private final ExcelRepository repo = ExcelRepository.get();

    public void pick(
            String upc,
            Location location,
            int qty
    ) {
        if (!location.isOutboundArea()) {
            throw new RuntimeException("Outbound hanya dari R-area");
        }

        List<StockBatch> available = repo.getAllStock().stream()
                .filter(b -> b.upc.equals(upc))
                .filter(b -> b.location.equals(location))
                .filter(b -> b.quantity > 0)
                .sorted(Comparator.comparing(b -> b.batchDate))
                .collect(Collectors.toList());

        int remain = qty;

        for (StockBatch b : available) {
            if (remain <= 0) break;

            int take = Math.min(b.quantity, remain);
            remain -= take;

            repo.log(
                    "OUTBOUND",
                    b.upc,
                    b.sku,
                    take,
                    location,
                    null
            );
        }

        if (remain > 0) {
            throw new RuntimeException("Stok outbound tidak mencukupi");
        }
    }
}
