package model;

import model.type.LockType;

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

    public void setCurLock(Lock currentLock) {
        curLock = currentLock;
    }

    public boolean isTxHoldReadLock(String txId) {
        return readLocks.contains(txId);
    }

    public HashSet getReadLocks() {return readLocks;}

    public void setReadLock(String txId) {
        readLocks.add(txId);
    }

    public void releaseReadLock(String txId) {

        if (!readLocks.contains(txId)) return;
        readLocks.remove(txId);
    }

    public void promoteFromReadLockToWriteLock(String varName, String txId) {
        readLocks.remove(txId);
        curLock = new Lock(varName, txId, LockType.WRITE);
    }

}