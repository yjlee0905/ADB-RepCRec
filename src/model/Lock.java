/**
 * @author Yunjeon Lee, Kyu Doun Sim
 * @date Nov 27th - Dec 1st, 2022
 */

package model;

import model.type.LockType;

public class Lock {
    private String txId;
    private String variableName;
    private LockType lockType;
    private Long timestamp;


    /**
     * Constructor for Lock.
     *
     * No side effect other than allocating memory for the Lock
     *
     * @param txId
     * @param variableName
     * @param lockType
     * @param timestamp
     */
    public Lock(String txId, String variableName, LockType lockType, Long timestamp) {
        this.txId = txId;
        this.variableName = variableName;
        this.lockType = lockType;
        this.timestamp = timestamp;
    }

    /**
     * Accessor for Transaction ID
     *
     * No side effect
     *
     * @return String
     */
    public String getTxId() {
        return this.txId;
    }

    /**
     * Accessor for the Lock's Type
     *
     * No side effect
     *
     * @return LockType
     */
    public LockType getLockType() {
        return this.lockType;
    }

    /**
     * Accessor for the Lock's Timestamp
     * No side effect
     *
     * @return Long
     */
    public Long getTimestamp() {return this.timestamp;}
}
