package model;

import model.type.LockType;

import java.util.HashSet;

public class LockTable {

    private Lock curLock;
    private HashSet<String> readLocks;


    /**
     * Default Constructor for LockTable
     *
     * No side effect other than allocating memory
     * for the LockTable
     *
     * @param curLock the current Lock that it is holding
     */
    public LockTable(Lock curLock) {
        this.curLock = curLock;
        this.readLocks = new HashSet();
    }

    /**
     * Constructor for LockTable when there are also
     * readLocks allocated.
     *
     * No side effect other than allocating memory
     * for the LockTable
     *
     *
     * @param curLock
     * @param readLocks
     */
    public LockTable(Lock curLock, HashSet<String> readLocks) {
        this.curLock = curLock;
        this.readLocks = readLocks;
    }

    /**
     * Accessor for CurLock
     *
     * No side effects
     *
     * @return Lock return the current lock
     */
    public Lock getCurLock() {return curLock;}

    /**
     * Setter for CurLock
     *
     * No side effects
     *
     * @param currentLock the Lock that will change the
     *                    current Lock
     */
    public void setCurLock(Lock currentLock) {
        curLock = currentLock;
    }

    /**
     * A function that returns true if a transaction is
     * in the readLocks HashSet
     *
     * No side effects
     *
     * @param txId
     * @return boolean if a transaction is in the readLocks
     */
    public boolean isTxHoldReadLock(String txId) {
        return readLocks.contains(txId);
    }

    /**
     * Accessor for readLocks
     *
     * No side effects
     *
     * @return readLocks a HashSet of readLocks
     */

    public HashSet getReadLocks() {return readLocks;}


    /**
     * A function that will add
     * a transaction string to readLocks
     *
     * The side effect is that it will add a string
     * to the readLocks.
     *
     * @param txId
     * @return void
     */
    public void setReadLock(String txId) {
        readLocks.add(txId);
    }

    /**
     * A function that will remove a transaction
     * from the readLocks
     *
     * Has a side effect that it will remove a string
     * of txId from readLocks as long as it exists
     *
     * @param txId
     */
    public void releaseReadLock(String txId) {

        if (!readLocks.contains(txId)) return;
        readLocks.remove(txId);
    }

    /**
     * A function that will promote a read lock to write lock that are
     * presented by the parameters
     *
     * Has a side effect of removing a specific transaction from the readLocks
     * and will create a current Lock with the type of write with the provided
     * variable name and timestamp
     *
     * @param varName
     * @param txId
     * @param timestamp
     */
    public void promoteFromReadLockToWriteLock(String varName, String txId, Long timestamp) {
        readLocks.remove(txId);
        curLock = new Lock(txId, varName, LockType.WRITE, timestamp);
    }

}