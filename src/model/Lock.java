package model;

public class Lock {
    private String txId;
    private String variableName;
    private LockType lockType;

    public Lock(String txId, String variableName, LockType lockType) {
        this.txId = txId;
        this.variableName = variableName;
        this.lockType = lockType;
    }

    public String getTxId() {
        return this.txId;
    }

    public LockType getLockType() {
        return this.lockType;
    }
}
