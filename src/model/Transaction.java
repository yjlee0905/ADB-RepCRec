/**
 * @author Yunjeon Lee, Kyu Doun Sim
 * @date Nov 27th - Dec 1st, 2022
 */

package model;

import java.util.HashSet;

public class Transaction {

    private final String txId;
    private final Long timestamp;
    private boolean isAborted;
    private HashSet sitesVisited;

    /**
     * Constructor for Transaction
     *
     * No side effect other than allocating memory
     * for the Transaction
     *
     * @param txId
     * @param timestamp
     */
    public Transaction(String txId, Long timestamp) {
        this.txId = txId;
        this.timestamp = timestamp;
        this.sitesVisited = new HashSet();
    }


    /**
     * Accessor of the transaction ID
     *
     * No side effect
     *
     * @return String of transaction ID
     */
    public String getTxId() {
        return txId;
    }

    /**
     * Accessor of the Timestamp
     *
     * No side effect
     *
     * @return Long timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Accessor of the abortion status
     *
     * No side effect
     *
     * @return boolean indicating if a transaction is aborted or not
     */
    public boolean isAborted() {return this.isAborted;}

    /**
     * Setter of the abortion status
     *
     * A side effect of modifying the abortion status of the current class
     *
     * @param isAborted a boolean that will change the status as is
     */
    public void setIsAborted(boolean isAborted) {
        this.isAborted = isAborted;
    }

    /**
     * Accessor of the SitesVisited
     *
     * No side effects
     *
     * @return HashSet of the sitesVisited variable
     */
    public HashSet getVisitedSites() {return this.sitesVisited;}

    /**
     * A function that adds a specific siteID to the
     * sitesVisited variable
     *
     * Has a side effect of adding a site ID to the
     * sitesVisited variable
     *
     * @param siteId
     */
    public void addSitesVisited(Integer siteId) {
        this.sitesVisited.add(siteId);
    }
}