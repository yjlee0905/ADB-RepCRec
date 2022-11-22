package model;

public class Variable {
    private Integer version;
    private Long commitTime;
    private Integer value;

    public Variable(Integer value, Long commitTime) {
        this.value = value;
        this.commitTime = commitTime;
        this.version = 0;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Long commitTime) {
        this.commitTime = commitTime;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
