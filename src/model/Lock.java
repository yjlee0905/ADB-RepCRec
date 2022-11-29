package model;

import model.type.LockType;

public class Lock {
    private String txId;
    private String variableName;
    private LockType lockType;
    private Long timestamp;

    public Lock(String txId, String variableName, LockType lockType, Long timestamp) {
        this.txId = txId;
        this.variableName = variableName;
        this.lockType = lockType;
        this.timestamp = timestamp;
    }

    public String getTxId() {
        return this.txId;
    }

    public LockType getLockType() {
        return this.lockType;
    }

    public Long getTimestamp() {return this.timestamp;}
}
