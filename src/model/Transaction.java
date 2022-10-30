package model;

public class Transaction {

    private final Integer txId;
    private final String name;
    private final Long timestamp;

    public Transaction(Integer txId, String name, Long timestamp) {
        this.txId = txId;
        this.name = name;
        this.timestamp = timestamp;
    }

    public Integer getTxId() {
        return txId;
    }

    public String getName() {
        return name;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}