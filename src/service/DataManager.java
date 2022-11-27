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

    private Map<String, List<Lock>> lockWaitingList = new HashMap<>(); // key: variable, value: currently, first TxId has lock

    private boolean isUp;

    private Map<String, Variable> variables = new HashMap<>(); // commited value

    private Map<String, Variable> tempVars = new HashMap<>(); // key: variable, value: Variable

    private Map<String, List<History>> commitHistories = new HashMap<>(); // key: variable

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

    public Integer getId() {return this.id;}

    public Map<String, LockTable> getCurLock() { return curLock;}

    public Map<String, List<Lock>> getLockWaitingList() {return this.lockWaitingList;}


//    public Integer read(String varName) {
//        return variables.get(varName).getValue();
//    }

    public Integer read(String varName, String txId) {
        Variable variable = variables.get(varName);
        if (!variable.canRead()) {
            return null;
        }

        if (!curLock.containsKey(varName) || curLock.get(varName).getCurLock() == null) {
            Lock readLock = new Lock(txId, varName, LockType.READ);
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

                // TODO need write lock check
            } else {
                LockTable curLockTable = curLock.get(varName);
                curLockTable.setReadLock(txId);
                return variable.getValue();
            }

        } else if (curLockForVar.getLockType().equals(LockType.WRITE)) {
            if (curLockForVar.getTxId().equals(txId)) {
                // read temp value
                return variables.get(varName).versionedVal.get(txId);
            } else {
                // TODO add lock queue
                // updateWriteLockWaitingList(varName, );
                return null;
            }
        }

//        // TODO shared lock in OH
//        if (!curLock.containsKey(varName) ||
//                (curLock.get(varName).equals(txId)) && curLock.get(varName).getCurLock().getLockType().equals(LockType.READ)) {
//            Lock readLock = new Lock(txId, varName, LockType.READ);
//            curLock.put(varName, readLock);
//            return variable.getValue();
//        }

        return null;
    }

    public void write(String varName, Integer value, Long timestamp, String txId) {
        if (!curLock.containsKey(varName) || curLock.get(varName) == null || curLock.get(varName).getCurLock() == null) {
            Lock lock = new Lock(txId, varName, LockType.WRITE);
            LockTable lockTable = new LockTable(lock);
            curLock.put(varName, lockTable);

            Variable var = tempVars.get(varName);
            var.setTempValueWithTxId(txId, value);
        } else if (curLock.get(varName).getCurLock().getLockType().equals(LockType.WRITE) && curLock.get(varName).getCurLock().getTxId().equals(txId)) {
            Variable var = tempVars.get(varName);
            var.setTempValueWithTxId(txId, value);
        } else {
            System.out.println("promote read to write");
        }
        // TODO check replicated variable changed to isRead > X 아닌 듯?
    }

    public void updateWriteLockWaitingList(String varName, Integer value, Long timestamp, String txId) {
        Lock targetLock = new Lock(txId, varName, LockType.WRITE);
        if (lockWaitingList.containsKey(varName)) {
            lockWaitingList.get(varName).add(targetLock);
        } else {
            List<Lock> locks = new ArrayList<>();
            locks.add(targetLock);
            lockWaitingList.put(varName, locks);
        }
    }

    public boolean isExistVariable(String variableName) {
        return this.variables.containsKey(variableName);
    }

    public boolean isUp() {return this.isUp;}

    public boolean setIsUp(boolean isUp) {
        this.isUp = isUp;
        return this.isUp;
    }

    public boolean isWriteLockAvailable(String txId, String variableName) {
        if (!this.curLock.containsKey(variableName) || this.curLock.get(variableName).getCurLock() == null) return true;
        return false;
    }

    public void clearLockTable() {
        this.curLock.clear();
        //this.lockWaitingList.clear();
    }

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

    public void processCommit(String txId, Long timestamp) {
        for (String varName: tempVars.keySet()) {
            // 여기서 versionedVal 봐야함
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

        clearTxId(txId);

        // TODO update lock table
    }

    public void clearTxId(String txId) {
        // remove current lock for txId
        //curLock.entrySet().removeIf(entry -> entry.getValue().getCurLock().getTxId().equals(txId));

        for (String varName: curLock.keySet()) {
            LockTable lockTable = curLock.get(varName);

            if (lockTable.getCurLock() == null) return;

            Lock currentLock = lockTable.getCurLock();
            if (currentLock.getLockType() == LockType.WRITE && currentLock.getTxId().equals(txId)) {
                lockTable.setCurLock(null);
            } else if (currentLock.getLockType() == LockType.READ) {
                lockTable.releaseReadLock(txId);
                if (lockTable.getReadLocks() == null || lockTable.getReadLocks().size() == 0) {
                    lockTable.setCurLock(null);
                }
            }
        }

        // TODO update locktable
    }

    public void clearTxIdFromLockWaitingList(String txId) {
        // remove transaction from lock wait list
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

//                for (History failHis: failHistory) {
//                    if (lastCommit.getTimestamp() <= failHis.getTimestamp() && failHis.getTimestamp() <= readOnlyStartTime) {
//                        return null;
//                    }
//                }
            }
        }
        return null;

//            for (History history: variableHistory) { // TODO check whether last or all, last부터 check?
//                if (!variable.isReplicated()) {
//                    if (history.getTimestamp() <= readOnlyStartTime) {
//                        return history.getSnapshotValue();
//                    }
//                    return null;
//                } else { // replicated TODO implement
//                    if (failHistory == null || failHistory.size() == 0)
//                        return snapshot.getValue();
//                    for (History failHis: failHistory) {
//                        if (failHis.getTimestamp() <= readOnlyStartTime)
//                            return null;
//                    }
//                    return snapshot.getValue();
//                }
//            }
    }

    public void setVariablesIsRead(boolean isRead) {
        for (String varName: variables.keySet()) {
            Variable variable = variables.get(varName);
            // if variable is not replicated, this can be read
            if (variable.isReplicated()) variable.setIsRead(isRead);
        }
    }

}
