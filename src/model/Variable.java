package model;

public class Variable {
    private Integer version;
    private Long commitTime;
    private String committedBy;
    private Integer value;

    public Variable(Integer value, Long commitTime, String committedBy) {
        this.value = value;
        this.commitTime = commitTime;
        this.committedBy = committedBy;
        this.version = 0;
    }

    public Integer getVersion() {
        return this.version;
    }

    public Long getCommitTime() {
        return this.commitTime;
    }

    public void setCommitTime(Long commitTime) {
        this.commitTime = commitTime;
    }

    public String getCommittedBy() {return this.committedBy;}

    public void setCommittedBy(String txId) {
        this.committedBy = txId;
    }

    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
        this.version++;
    }
}
