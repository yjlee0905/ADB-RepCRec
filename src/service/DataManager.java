package service;

import model.History;
import model.Lock;
import model.type.LockType;
import model.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private Integer id;

    private Map<String, List<Lock>> lockTable = new HashMap<>(); // key: variable, value: currently, first TxId has lock

    private boolean isUp;

    private Map<String, Variable> variables = new HashMap<>(); // commited value

    private Map<String, Variable> tempVars = new HashMap<>(); // key: variable, value: Variable

    private Map<String, List<History>> commitHistories = new HashMap<>(); // key: variable

    //  txId <var, Variable>
//    private Map<String, Map<String, Variable>> tempVars = new HashMap<>();

    public DataManager(Integer id, Long time) {
        this.id = id;
        this.isUp = true;

        for (int i = 1; i <= 10; i++) {
            String variableName = "x" + i * 2;
            Variable variable = new Variable(i*2*10, time, "init", true);
            this.variables.put(variableName, variable);
            this.tempVars.put(variableName, variable);

            List<History> commitHistory = new ArrayList<>();
            History history = new History(id, variableName, "init", time);
            commitHistory.add(history);
            this.commitHistories.put(variableName, commitHistory);
//            Map<String, Variable> temp = new HashMap<>();
//            temp.put(variableName, variable);
//            this.tempVars.put(variableName, temp);
        }

        if (id % 2 == 0) {
            String variableName1 = "x" + (id - 1);
            Variable variable1 = new Variable((id - 1)*10, time, "init", false);
            this.variables.put(variableName1, variable1);
            this.tempVars.put(variableName1, variable1);

            List<History> commitHistory1 = new ArrayList<>();
            History history1 = new History(id, variableName1, "init", time);
            commitHistory1.add(history1);
            this.commitHistories.put(variableName1, commitHistory1);
//            Map<String, Variable> temp1 = new HashMap<>();
//            temp1.put(variableName1, variable1);
//            this.tempVars.put(variableName1, temp1);

            String variableName2 = "x" + (id - 1 + 10);
            Variable variable2 = new Variable((id - 1 + 10)*10, time, "init", false);
            this.variables.put(variableName2, variable2);
            this.tempVars.put(variableName2, variable2);

            List<History> commitHistory2 = new ArrayList<>();
            History history2 = new History(id, variableName2, "init", time);
            commitHistory2.add(history2);
            this.commitHistories.put(variableName1, commitHistory2);
//            Map<String, Variable> temp2 = new HashMap<>();
//            temp2.put(variableName2, variable2);
//            this.tempVars.put(variableName2, temp2);
        }
    }

    public Integer getId() {return this.id;}

    public Map<String, List<Lock>> getLockTable() {return this.lockTable;}

    public Integer read(String varName) {
        return variables.get(varName).getValue();
    }

    public void write(String varName, Integer value, Long timestamp, String txId) {
        Variable var = tempVars.get(varName);
        var.setTempValueWithTxId(txId, value);
        updateLockTable(varName, value, timestamp, txId);




        //        Map<String, Variable> varsFromTxId = this.tempVars.get(txId);
//
//        if (varsFromTxId == null) {
//
//
//        } else {
//            Variable original = varsFromTxId.get(varName);
//            original.setValue(value);
//            original.setCommitTime(timestamp);
//            original.setCommittedBy(txId);
//            varsFromTxId.put(varName, original);
//            this.tempVars.put(txId, varsFromTxId);
//        }
    }

    public void updateLockTable(String varName, Integer value, Long timestamp, String txId) {
        // get write lock
        Lock curLock = new Lock(txId, varName, LockType.WRITE);
        if (lockTable.containsKey(varName)) {
            lockTable.get(varName).add(curLock);
        } else {
            List<Lock> locks = new ArrayList<>();
            locks.add(curLock);
            lockTable.put(varName, locks);
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
        if (!this.lockTable.containsKey(variableName)) return true;

        List<Lock> lockInfo = this.lockTable.get(variableName);
        if (lockInfo.size() == 0 ||
                (lockInfo.get(0).getLockType() == LockType.WRITE && lockInfo.get(0).getTxId().equals(txId)) ) {return true;}
//        for(Lock singleLock : lockInfo) {
//            if (singleLock.getLockType() == LockType.WRITE) {
//                if (singleLock.getTxId().equals(txId)) return true;
//                // if not TODO implement
//                return false;
//            }
//        }

        // TODO implement
        return false;
    }

    public void clearLockTable() {
        this.lockTable.clear();
    }

    public void showVariables() {
        for (String varName: this.variables.keySet()) {
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
                    variables.put(varName, origin);

                    // add commit history
                    List<History> commitHistory = commitHistories.get(varName);
                    History newHistory = new History(id, varName, txId, timestamp);
                    commitHistory.add(newHistory);
                    commitHistories.put(varName, commitHistory);
                }
            }
        }

        clearTxId(txId);

//        Map<String, Variable> varsFromTxId = tempVars.get(txId);
//        for (String varName: varsFromTxId.keySet()) {
//            Variable origin = variables.get(varName);
//            Integer updatedValue = tempVars.get(txId).get(varName).getValue();
//            origin.setValue(updatedValue);
//            origin.setCommitTime(timestamp);
//            origin.setCommittedBy(txId);
//            variables.put(varName, origin);
//        }
    }

    public void clearTxId(String txId) {
        lockTable.entrySet().removeIf(entry -> entry.getValue().equals(txId));
    }

    public Variable getSnapshot(String varName, Long readOnlyStartTime, List<History> failHistory) {
        Variable variable = variables.get(varName);
        if (variable.canRead()) {
            List<History> variableHistory = commitHistories.get(varName);
            for (History history: variableHistory) { // TODO check whether last or all
                if (!variable.isReplicated()) {
                    if (history.getTimestamp() <= readOnlyStartTime) {
                        return variable;
                    }
                    return null;
                } else { // replicated
                    if (failHistory == null || failHistory.size() == 0) return variable;
                    for (History failHis: failHistory) {
                        if (failHis.getTimestamp() <= readOnlyStartTime) return null;
                    }
                    return variable;
                }
            }
        }
        return null;
    }

    public void setVariablesIsRead(boolean isRead) {
        for (String varName: variables.keySet()) {
            Variable variable = variables.get(varName);
            variable.setIsRead(isRead);
        }
    }

}
