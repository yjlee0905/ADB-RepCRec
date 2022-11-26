package service;

import model.History;
import model.Lock;
import model.Variable;
import model.type.LockType;

import java.util.*;

public class DataManager {

    private Integer id;

    private Map<String, Lock> curLock = new HashMap<>(); // key: variable, value: transaction

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
            History history = new History(id, variableName, "init", time);
            commitHistory.add(history);
            this.commitHistories.put(variableName, commitHistory);
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

            String variableName2 = "x" + (id - 1 + 10);
            Variable variable2 = new Variable((id - 1 + 10)*10, time, "init", false);
            this.variables.put(variableName2, variable2);
            this.tempVars.put(variableName2, variable2);

            List<History> commitHistory2 = new ArrayList<>();
            History history2 = new History(id, variableName2, "init", time);
            commitHistory2.add(history2);
            this.commitHistories.put(variableName1, commitHistory2);
        }
    }

    public Integer getId() {return this.id;}

    public Map<String, List<Lock>> getLockWaitingList() {return this.lockWaitingList;}

    public Integer read(String varName) {
        return variables.get(varName).getValue();
    }

    public void write(String varName, Integer value, Long timestamp, String txId) {
        Variable var = tempVars.get(varName);
        var.setTempValueWithTxId(txId, value);

        Lock lock = new Lock(txId, varName, LockType.WRITE);
        curLock.put(varName, lock);
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
        if (!this.curLock.containsKey(variableName) || this.curLock.get(variableName) == null) return true;
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
    }

    public void clearTxId(String txId) {
        // remove current lock for txId
        curLock.entrySet().removeIf(entry -> entry.getValue().getTxId().equals(txId));

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
