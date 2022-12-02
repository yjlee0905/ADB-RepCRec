package service;

import model.History;
import model.Lock;
import model.LockTable;
import model.Variable;
import model.type.LockType;

import java.util.*;

public class DataManager {

    private Integer id;

    private Map<String, LockTable> curLock = new HashMap<>(); // key: variable, value: LockTable

    // locks are acquired in first-come-first-serve fashion
    private Map<String, List<Lock>> lockWaitingList = new HashMap<>(); // key: variable, value: lock waiting list for the variable

    private boolean isUp;

    private Map<String, Variable> variables = new HashMap<>(); // commited value

    private Map<String, Variable> tempVars = new HashMap<>(); // key: variable, value: Variable, values that are not committed yet

    private Map<String, List<History>> commitHistories = new HashMap<>(); // key: variable

    // initialize site in constructor
    public DataManager(Integer id, Long time) {
        this.id = id;
        this.isUp = true;

        for (int i = 1; i <= 10; i++) {
            String variableName = "x" + i * 2;
            Variable variable = new Variable(i*2*10, time, "init", true);
            this.variables.put(variableName, variable);
            this.tempVars.put(variableName, variable);

            List<History> commitHistory = new ArrayList<>();
            History history = new History(id, variableName, i*2*10, "init", time);
            commitHistory.add(history);
            this.commitHistories.put(variableName, commitHistory);
        }

        if (id % 2 == 0) {
            String variableName1 = "x" + (id - 1);
            Variable variable1 = new Variable((id - 1)*10, time, "init", false);
            this.variables.put(variableName1, variable1);
            this.tempVars.put(variableName1, variable1);

            List<History> commitHistory1 = new ArrayList<>();
            History history1 = new History(id, variableName1, (id - 1)*10, "init", time);
            commitHistory1.add(history1);
            this.commitHistories.put(variableName1, commitHistory1);

            String variableName2 = "x" + (id - 1 + 10);
            Variable variable2 = new Variable((id - 1 + 10)*10, time, "init", false);
            this.variables.put(variableName2, variable2);
            this.tempVars.put(variableName2, variable2);

            List<History> commitHistory2 = new ArrayList<>();
            History history2 = new History(id, variableName2, (id - 1 + 10)*10, "init", time);
            commitHistory2.add(history2);
            this.commitHistories.put(variableName2, commitHistory2);
        }
    }

    /**
     * Accessor for site ID
     * No side effect
     * @return Integer
     */
    public Integer getId() {return this.id;}

    /**
     * Accessor for current lock table
     * No side effect
     * @return Map<String, LockTable>
     */
    public Map<String, LockTable> getCurLock() { return curLock;}

    /**
     * Accessor for lock waiting list
     * No side effect
     * @return Map<String, List<Lock>>
     */
    public Map<String, List<Lock>> getLockWaitingList() {return this.lockWaitingList;}

    /**
     * This method process READ operation
     * If the variable is not readable because of the site down, return null
     * If there is no lock for variable or the transaction already holds READ lock of the variable, we can read the value directly.
     * If current lock for variable is READ lock and there is no WRITE lock waiting in the wait list, transaction txId can hold READ lock and read the value,
     * but if there is a WRITE lock waiting in the wait list, transaction txId should wait.
     * If transaction txId already holds WRITE lock for the variable, we can read temporary value which is not committed yet.
     * If other transaction holds WRITE lock for the variable, the READ transaction txId should wait.
     * @param varName String
     * @param txId String
     * @param timestamp Long
     * @return value read from the site, return null if READ operation is not possible
     * */
    public Integer read(String varName, String txId, Long timestamp) {
        Variable variable = variables.get(varName);
        if (!variable.canRead()) {
            System.out.println("Site: " + id + " was down and is just recovered, so the value cannot be read at this time.");
            return null;
        }

        if (!curLock.containsKey(varName) || curLock.get(varName).getCurLock() == null) {
            Lock readLock = new Lock(txId, varName, LockType.READ, timestamp);
            HashSet<String> readLocks = new HashSet<>();
            readLocks.add(txId);
            LockTable lockTable = new LockTable(readLock, readLocks);
            curLock.put(varName, lockTable);
            return variable.getValue();
        }

        Lock curLockForVar = curLock.get(varName).getCurLock();
        if (curLockForVar.getLockType().equals(LockType.READ)) {
            if (curLock.get(varName).isTxHoldReadLock(txId)) {
                return variable.getValue();

            } else {
                // TODO need write lock check
                if (checkWriteLockInWaitingList(varName)) {
                    addToLockWaitingList(varName, txId, LockType.READ, timestamp);
                    System.out.println("Read lock conflict at site: " + id);
                    return null;
                } else {
                    LockTable curLockTable = curLock.get(varName);
                    if (curLockTable.getCurLock().getLockType().equals(LockType.READ)) {
                        curLockTable.setReadLock(txId);
                    }
                    return variable.getValue();
                }

            }

        } else if (curLockForVar.getLockType().equals(LockType.WRITE)) {
            if (curLockForVar.getTxId().equals(txId)) {
                // read temp value
                return variables.get(varName).versionedVal.get(txId);
            } else {
                addToLockWaitingList(varName, txId, LockType.READ, timestamp);
                System.out.println("Read lock conflict at site: " + id);
                return null;
            }
        }
        return null;
    }

    /**
     * This method process WRITE operation
     * If there is no current lock for variable or the transaction txId already holds WRITE lock for the variable, write the value to tempVars (because this is not committed).
     * If the transaction txId holds READ lock for the variable and there is no other WRITE lock waiting for the variable,
     * promote the READ lock to WRITE lock and write the value to tempVars (because this is not committed).
     * @param varName String
     * @param value Integer
     * @param timestamp Long
     * @param txId String
     * @return no return
     * */
    public void write(String varName, Integer value, Long timestamp, String txId) {
        if (!curLock.containsKey(varName) || curLock.get(varName) == null || curLock.get(varName).getCurLock() == null) {
            Lock lock = new Lock(txId, varName, LockType.WRITE, timestamp);
            LockTable lockTable = new LockTable(lock);
            curLock.put(varName, lockTable);

            Variable var = tempVars.get(varName);
            var.setTempValueWithTxId(txId, value);
        } else if (curLock.get(varName).getCurLock().getLockType().equals(LockType.WRITE) && curLock.get(varName).getCurLock().getTxId().equals(txId)) {
            Variable var = tempVars.get(varName);
            var.setTempValueWithTxId(txId, value);
        } else if (curLock.get(varName).getCurLock().getLockType().equals(LockType.READ)) {
            LockTable lockInfo = curLock.get(varName);
            HashSet<String> curReadLocks = lockInfo.getReadLocks();

            if (curReadLocks.size() == 1 && curReadLocks.contains(txId) && !checkOtherWriteLockInWaitingList(varName, txId)) {
                lockInfo.promoteFromReadLockToWriteLock(varName, txId, timestamp);
                Variable var = tempVars.get(varName);
                var.setTempValueWithTxId(txId, value);
                System.out.println("promote from READ lock to WRITE lock");
            }
        }
    }

    /**
     * Check whether there is a WRITE lock request waiting for the variable "varName".
     * @param varName String
     * @return boolean
     * */
    public boolean checkWriteLockInWaitingList(String varName) {
        List<Lock> lockWaitListForVar = lockWaitingList.get(varName);

        if (lockWaitListForVar == null) return false;

        for (Lock lock: lockWaitListForVar) {
            if (lock.getLockType().equals(LockType.WRITE)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Check whether there is a WRITE lock request waiting for the variable "varName" other than transaction "txId"
     * @param varName String
     * @param txId String
     * @return boolean
     * */
    public boolean checkOtherWriteLockInWaitingList(String varName, String txId) {
        List<Lock> lockWaitListForVar = lockWaitingList.get(varName);

        if (lockWaitListForVar == null) return false;

        for (Lock lock: lockWaitListForVar) {
            if (lock.getLockType().equals(LockType.WRITE) && !lock.getTxId().equals(txId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add lock to the lock waiting list except when current transaction already has READ lock waiting in waiting list
     * or the same lock type is in the lock waiting list
     * @param varName String
     * @param txId String
     * @param lockType LockType
     * @param timestamp Long
     * @return no return
     * */
    public void addToLockWaitingList(String varName, String txId, LockType lockType, Long timestamp) {
        List<Lock> lockWaitListForVar = lockWaitingList.get(varName);

        if (lockWaitListForVar == null) {
            Lock newLock = new Lock(txId, varName, lockType, timestamp);
            List<Lock> newLockWaitList = new ArrayList<>();
            newLockWaitList.add(newLock);
            lockWaitingList.put(varName, newLockWaitList);
            return;
        }

        for (Lock lock: lockWaitListForVar) {
            if (lock.getTxId().equals(txId) && (lock.getLockType().equals(lockType)) || lock.getLockType().equals(LockType.READ)) {
                return;
            }
        }

        Lock newLock = new Lock(txId, varName, lockType, timestamp);
        lockWaitListForVar.add(newLock);
        lockWaitingList.put(varName, lockWaitListForVar);
    }

    /**
     * Check whether the site has variable or not
     * @param variableName String
     * @return boolean
     * */
    public boolean isExistVariable(String variableName) {
        return this.variables.containsKey(variableName);
    }

    /**
     * Accessor for isUp
     * No side effect
     * @return boolean
     */
    public boolean isUp() {return this.isUp;}

    /**
     * Setter for isUp
     * @param isUp boolean
     * @return boolean
     */
    public boolean setIsUp(boolean isUp) {
        this.isUp = isUp;
        return this.isUp;
    }


    /**
     * Check whether the transaction can hold WRITE lock or not
     * If transaction txId holds READ lock for the variable and there is no WRITE lock waiting in the wait list, return true
     * If transaction txId holds WRITE lock for the variable, return true
     * Other cases, return false
     * @param txId String
     * @param variableName String
     * @param timestamp Long
     * @return boolean
     * */
    public boolean isWriteLockAvailable(String txId, String variableName, Long timestamp) {
        if (!this.curLock.containsKey(variableName) || this.curLock.get(variableName).getCurLock() == null) return true;

        LockTable lockForVar = curLock.get(variableName);
        Lock currentLockForVar = lockForVar.getCurLock();

        if (currentLockForVar.getLockType().equals(LockType.READ)) {
            if (lockForVar.getReadLocks().size() > 1) {
                // add to lock waiting list
                addToLockWaitingList(variableName, txId, LockType.WRITE, timestamp);
                return false;
            } else {
                if (lockForVar.getReadLocks().contains(txId)) {
                    if (!checkOtherWriteLockInWaitingList(variableName, txId)) {
                        return true;
                    } else {
                        addToLockWaitingList(variableName, txId, LockType.WRITE, timestamp);
                        return false;
                    }
                } else {
                    // add to lock waiting list
                    addToLockWaitingList(variableName, txId, LockType.WRITE, timestamp);
                    return false;
                }
            }
        } else if (currentLockForVar.getLockType().equals(LockType.WRITE)) {
            if (currentLockForVar.getTxId().equals(txId)) {
                return true;
            } else {
                addToLockWaitingList(variableName, txId, LockType.WRITE, timestamp);
                return false;
            }
        }
        return false;
    }

    /**
     * clear te current lockTable
     * no param and no return
     * */
    public void clearLockTable() {
        this.curLock.clear();
        //this.lockWaitingList.clear();
    }

    /**
     * show all the current values of variables from the site
     * no param and no return
     * */
    public void showVariables() {
        List<Integer> varIds = new ArrayList<>();
        for (String varName: this.variables.keySet()) {
            varIds.add(Integer.valueOf(varName.substring(1)));
        }
        Collections.sort(varIds);

        for (Integer varId: varIds) {
            String varName = 'x' + varId.toString();
            System.out.print(varName + ":" + this.variables.get(varName).getValue() + "  ");
        }
    }

    /**
     * @params String txId, Long timestamp
     * @return no return
     * */
    public void processCommit(String txId, Long timestamp) {
        for (String varName: tempVars.keySet()) {
            Variable variable = tempVars.get(varName);
            Map<String, Integer> versionedVal = variable.versionedVal;

            for (String versionedTxId: versionedVal.keySet()) {
                if (versionedTxId.equals(txId)) {
                    Variable origin = variables.get(varName);
                    origin.setValue(versionedVal.get(versionedTxId));
                    origin.setCommitTime(timestamp);
                    origin.setCommittedBy(txId);
                    origin.setIsRead(true);
                    variables.put(varName, origin);

                    // add commit history
                    List<History> commitHistory = commitHistories.get(varName);
                    History newHistory = new History(id, varName, origin.getValue(), txId, timestamp);
                    commitHistory.add(newHistory);
                    commitHistories.put(varName, commitHistory);
                }
            }

            // temporary value remove
            versionedVal.keySet().removeIf(key -> key.equals(txId));
        }

        clearTxId(txId, timestamp);

        // TODO update lock table
        // updateCurLock(timestamp);
    }

    /**
     * release lock with txId : if transaction has READ lock, also release the shared lock
     * after release, take next lock in the lock waiting list
     * @params String txId, Long timestamp
     * @return no return
     * */
    public void clearTxId(String txId, Long timestamp) {
        // remove current lock for txId
        //curLock.entrySet().removeIf(entry -> entry.getValue().getCurLock().getTxId().equals(txId));

        for (String varName: curLock.keySet()) {
            LockTable lockTable = curLock.get(varName);

            if (lockTable.getCurLock() == null) continue;

            Lock currentLock = lockTable.getCurLock();
            if (currentLock.getLockType() == LockType.WRITE && currentLock.getTxId().equals(txId)) {
                //System.out.println(id + "     " + txId + "before: " + lockTable.getCurLock());
                lockTable.setCurLock(null);
                //System.out.println(id + "     " + txId + "after: " + lockTable.getCurLock());

            } else if (currentLock.getLockType() == LockType.READ) {
                lockTable.releaseReadLock(txId);
                if (lockTable.getReadLocks() == null || lockTable.getReadLocks().size() == 0 || lockTable.getReadLocks().isEmpty()) {
                    lockTable.setCurLock(null);
                }
            }
        }

        // TODO update locktable
        updateCurLock(timestamp);
    }

    /**
     * take next lock waiting in the waiting list, and set the lock to the current lock
     * @param timestamp Long
     * @return no return
     * */
    private void updateCurLock(Long timestamp) {
        for (String varName: curLock.keySet()) {
            if (curLock.get(varName).getCurLock() != null || lockWaitingList.get(varName) == null || lockWaitingList.get(varName).size() == 0) continue;
            Lock firstWaitingLock = lockWaitingList.get(varName).get(0);
            lockWaitingList.get(varName).remove(firstWaitingLock);

            if (firstWaitingLock.getLockType().equals(LockType.READ)) {
                curLock.get(varName).setReadLock(firstWaitingLock.getTxId());
                curLock.get(varName).setCurLock(firstWaitingLock);
            } else if (firstWaitingLock.getLockType().equals(LockType.WRITE)) {
                curLock.get(varName).setCurLock(firstWaitingLock);
            }

            if (firstWaitingLock.getLockType().equals(LockType.READ) && lockWaitingList.get(varName).size() > 0) {
                List<Lock> lockWaiting = lockWaitingList.get(varName);
                List<Lock> toBeRemoved = new ArrayList<>();
                Lock nextLock = lockWaiting.get(0);
                for (int i = 0; i < lockWaiting.size(); i++) {
                    nextLock = lockWaiting.get(i);
                    if (nextLock.getLockType().equals(LockType.READ)) {
                        // add to read lock
                        curLock.get(varName).setReadLock(nextLock.getTxId());
                        // remove nextLock
                        toBeRemoved.add(nextLock);
                    } else {
                        break;
                    }
                }
                lockWaiting.removeAll(toBeRemoved);

                if (curLock.get(varName).getReadLocks().size() == 1 && curLock.get(varName).getReadLocks().contains(nextLock.getTxId())) {
                    // promote
                    curLock.get(varName).promoteFromReadLockToWriteLock(varName, nextLock.getTxId(), timestamp);
                    lockWaiting.remove(0);
                }
                lockWaitingList.put(varName, lockWaiting);

            }
        }
    }

    /**
     * remove transaction from lock wait list
     * @param txId String
     * @return void
     * */

    public void clearTxIdFromLockWaitingList(String txId) {
        for (String varName: lockWaitingList.keySet()) {
            List<Lock> toBeRemoved = new ArrayList<>();
            for (Lock lock: lockWaitingList.get(varName)) {
                if (lock.getTxId().equals(txId)) {
                    toBeRemoved.add(lock);
                }
            }
            lockWaitingList.get(varName).removeAll(toBeRemoved);
        }
    }


    /**
     * This function is for read-only transaction.
     * Get values of varName that has valid committed value before read-only transaction starts.
     * @params varName String, readOnlyStartTime Long, failHistories List<History>
     * @return Integer
     * */
    public Integer getSnapshot(String varName, Long readOnlyStartTime, List<History> failHistories) {
        // TODO works, but need to check during OH
        Variable variable = variables.get(varName);
        if (variable.canRead()) {
            List<History> variableHistory = commitHistories.get(varName);
            Collections.reverse(variableHistory);

            if (!variable.isReplicated()) {
                for (History history: variableHistory) {
                    if (history.getTimestamp() <= readOnlyStartTime) {
                        return history.getSnapshotValue();
                    }
                }
                return null;
            } else { // replicated
                if (failHistories == null || failHistories.size() == 0) {
                    return variable.getValue();
                }

                History lastCommit = commitHistories.get(varName).get(0);
                for (History failHistory: failHistories) {
                    if (lastCommit.getTimestamp() <= failHistory.getTimestamp() && failHistory.getTimestamp() <= readOnlyStartTime) {
                        return null;
                    }
                }
                return lastCommit.getSnapshotValue();

            }
        }
        return null;

    }

    /**
     * set isRead for all the variables
     * @param isRead boolean
     * @return no return
     * */
    public void setVariablesIsRead(boolean isRead) {
        for (String varName: variables.keySet()) {
            Variable variable = variables.get(varName);
            // if variable is not replicated, this can be read
            if (variable.isReplicated()) variable.setIsRead(isRead);
        }
    }

}
