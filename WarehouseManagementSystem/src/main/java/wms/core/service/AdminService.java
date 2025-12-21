package wms.core.service;

import wms.core.model.Location;
import wms.core.repository.ExcelRepository;

import java.util.List;

public class AdminService {

    private final ExcelRepository repo = ExcelRepository.get();

    public List<Location> getLocations() {
        return repo.getAllLocations();
    }

    public void addLocation(Location loc) {
        List<Location> all = repo.getAllLocations();
        if (all.contains(loc)) {
            throw new RuntimeException("Lokasi sudah ada");
        }
        repo.execute(wb -> {
            wb.getSheet("LOCATIONS")
                    .createRow(wb.getSheet("LOCATIONS").getLastRowNum() + 1)
                    .createCell(0)
                    .setCellValue(loc.toBarcode());
        });
    }

    public void updateLocation(Location oldLoc, Location newLoc) {
        if (!repo.getAllLocations().contains(oldLoc)) {
            throw new RuntimeException("Lokasi lama tidak ditemukan");
        }
        if (repo.getAllLocations().contains(newLoc)) {
            throw new RuntimeException("Lokasi baru sudah ada");
        }
        repo.updateLocation(oldLoc, newLoc);
    }

    public void deleteLocation(Location loc) {
        if (repo.isLocationUsed(loc)) {
            throw new RuntimeException("Lokasi masih digunakan oleh stok");
        }
        repo.deleteLocation(loc);
    }
}
