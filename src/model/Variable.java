package model;

import java.util.HashMap;
import java.util.Map;

public class Variable {

    private Integer value;
    private Long commitTime;
    private String committedBy;
    private boolean isRead;
    private boolean isWrite;
    private boolean isReplicated;
    public Map<String, Integer> versionedVal = new HashMap<>(); // key: txId, value: value


    public Variable(Integer value, Long commitTime, String committedBy, boolean isReplicated) {
        this.value = value;
        this.commitTime = commitTime;
        this.committedBy = committedBy;
        this.isRead = true;
        this.isWrite = true;
        this.isReplicated = isReplicated;
        this.versionedVal.put("init", value);
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
    }

    public boolean canRead() {return isRead;}

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isReplicated() {return isReplicated;}

    public void setTempValueWithTxId(String txId, Integer value) {
        versionedVal.put(txId, value);
        // TODO check need time or not
    }

    public Integer getFinalTempValueWithTxId(String txId) {
        return versionedVal.get(txId);
    }
}
