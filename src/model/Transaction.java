package model;

public class Transaction {

    private final String txId;
    private final Long timestamp;
    private boolean isAborted;

    public Transaction(String txId, Long timestamp) {
        this.txId = txId;
        this.timestamp = timestamp;
    }

    public String getTxId() {
        return txId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean isAborted() {return this.isAborted;}

    public void setIsAborted(boolean isAborted) {
        this.isAborted = isAborted;
    }
}