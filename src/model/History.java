package model;

public class History {

    private Integer siteId;
    private String variableName;
    private Integer value;
    private String committedBy;
    private Long timestamp;

    public History(Integer siteId, String varName, Integer value, String committedBy, Long timestamp) {
        this.siteId = siteId;
        this.variableName = varName;
        this.value = value;
        this.committedBy = committedBy;
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {return timestamp;}

    public Integer getSnapshotValue() {return value;}
}
