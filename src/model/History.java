package model;

public class History {

    private Integer siteId;
    private String variable;
    private String committedBy;
    private Long timestamp;

    public History(Integer siteId, String varName, String committedBy, Long timestamp) {
        this.siteId = siteId;
        this.variable = varName;
        this.committedBy = committedBy;
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {return timestamp;}
}
