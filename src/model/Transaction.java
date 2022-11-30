package model;

import java.util.HashSet;

public class Transaction {

    private final String txId;
    private final Long timestamp;
    private boolean isAborted;
    private HashSet sitesVisited;

    public Transaction(String txId, Long timestamp) {
        this.txId = txId;
        this.timestamp = timestamp;
        this.sitesVisited = new HashSet();
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

    public HashSet getVisitedSites() {return this.sitesVisited;}

    public void addSitesVisited(Integer siteId) {
        this.sitesVisited.add(siteId);
    }
}