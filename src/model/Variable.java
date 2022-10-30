package model;

public class Variable {
    private Integer version;
    private Long commitTime;

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
}
