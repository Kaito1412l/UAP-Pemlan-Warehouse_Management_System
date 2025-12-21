package wms.core.model;

import java.util.Objects;

public class Location {

    private final String area;
    private final String line;
    private final String rack;
    private final String level;
    private final String position;

    public Location(String area, String line, String rack, String level, String position) {
        this.area = area;
        this.line = line;
        this.rack = rack;
        this.level = level;
        this.position = position;
    }

    public String toBarcode() {
        return String.join("-", area, line, rack, level, position);
    }

    public boolean isInboundArea() {
        return area.startsWith("P");
    }

    public boolean isOutboundArea() {
        return area.startsWith("R");
    }

    public static Location fromBarcode(String code) {
        String[] p = code.split("-");
        if (p.length != 5) {
            throw new IllegalArgumentException("Format lokasi tidak valid");
        }
        return new Location(p[0], p[1], p[2], p[3], p[4]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location that = (Location) o;
        return toBarcode().equals(that.toBarcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toBarcode());
    }
}
