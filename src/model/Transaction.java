package model;

public class Transaction {

    private final String txId;
    private final Long timestamp;

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
}