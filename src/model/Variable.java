package model;

import java.util.HashMap;
import java.util.Map;

public class Variable {

    private Integer value;
    private Long commitTime;
    private String committedBy;
    private boolean isRead;
    private boolean isWrite;
    public Map<String, Integer> versionedVal = new HashMap<>(); // key: txId, value: value


    public Variable(Integer value, Long commitTime, String committedBy) {
        this.value = value;
        this.commitTime = commitTime;
        this.committedBy = committedBy;
        this.isRead = true;
        this.isWrite = true;
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

    public void setTempValueWithTxId(String txId, Integer value) {
        versionedVal.put(txId, value);
        // TODO check need time or not
    }

    public Integer getFinalTempValueWithTxId(String txId) {
        return versionedVal.get(txId);
    }
}
