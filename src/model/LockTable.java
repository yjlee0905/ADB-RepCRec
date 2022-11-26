package model;

import java.util.HashSet;

public class LockTable {

    private Lock curLock;
    private HashSet<String> readLocks;

    public LockTable(Lock curLock) {
        this.curLock = curLock;
        this.readLocks = new HashSet();
    }

    public LockTable(Lock curLock, HashSet<String> readLocks) {
        this.curLock = curLock;
        this.readLocks = readLocks;
    }

    public Lock getCurLock() {return curLock;}

    public boolean isTxHoldReadLock(String txId) {
        return readLocks.contains(txId);
    }

    public void setReadLock(String txId) {
        readLocks.add(txId);
    }

}