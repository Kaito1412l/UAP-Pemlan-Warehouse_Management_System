package wms.core.model;

public class Location {
    private final String area, line, rack, level, position;

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
}
