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


    /**
     * Constructor for Variable
     *
     * No other side effect other than allocating memory for constructing the
     * variable data
     *
     * @param value
     * @param commitTime
     * @param committedBy
     * @param isReplicated
     */
    public Variable(Integer value, Long commitTime, String committedBy, boolean isReplicated) {
        this.value = value;
        this.commitTime = commitTime;
        this.committedBy = committedBy;
        this.isRead = true;
        this.isWrite = true;
        this.isReplicated = isReplicated;
        this.versionedVal.put("init", value);
    }

    /**
     * Setter of the commit time
     *
     * A side effect of setting the current commitTime as the input
     * commit time
     *
     * @param commitTime
     */
    public void setCommitTime(Long commitTime) {
        this.commitTime = commitTime;
    }

    /**
     * Accessor of the committedBy
     *
     * No side effect
     *
     * @return String of the committed
     */
    public String getCommittedBy() {return this.committedBy;}

    /**
     * Setter of the committedBy
     *
     * Has a side effect of modifying the committedBy value as the parameter value
     *
     * @param txId
     */
    public void setCommittedBy(String txId) {
        this.committedBy = txId;
    }

    /**
     * Accessor of the value
     *
     * No side effect.
     *
     * @return Integer
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * Setter for the value
     *
     * Has a side effect of modifying the value
     *
     * @param value of Integer
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Accessor for the isRead variable
     *
     * No side effect
     *
     * @return boolean of IsRead
     */
    public boolean canRead() {return isRead;}

    /**
     * Setter of the IsRead variable
     *
     * Has a side effect of modifying the isRead variable as
     * the paramter input
     *
     * @param isRead boolean
     */
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    /**
     * Accessor of isReplicated variable
     *
     * No side effect
     *
     * @return boolean of the isReplicated variable
     */
    public boolean isReplicated() {return isReplicated;}

    /**
     * A function that will update the versionedVal Map as
     * the parameter inputs
     *
     * Has a side effect of updating the versionedVal variable
     * as a tuple of String, Integer
     *
     * @param txId
     * @param value
     */
    public void setTempValueWithTxId(String txId, Integer value) {
        versionedVal.put(txId, value);
        // TODO check need time or not
    }

}
