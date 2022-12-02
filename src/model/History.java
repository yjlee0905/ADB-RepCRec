package model;

public class History {

    private Integer siteId;
    private String variableName;
    private Integer value;
    private String committedBy;
    private Long timestamp;

    /**
     * Construct for History
     * The side effect is that it will allocate memory to create a History.
     *
     * @param siteId
     * @param varName
     * @param value
     * @param committedBy
     * @param timestamp
     */
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
