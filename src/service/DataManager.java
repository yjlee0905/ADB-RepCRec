package service;

import model.Lock;
import model.LockType;
import model.Variable;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private Integer id;

    private Map<String, Lock> lockTable = new HashMap<>();

    private boolean isUp;

    private Map<String, Variable> variables = new HashMap<>();

    public DataManager(Integer id, Long time) {
        this.id = id;
        this.isUp = true;

        for (int i = 1; i <= 10; i++) {
            String variableName = "x" + i * 2;
            Variable variable = new Variable(i*2*10, time, "init");
            this.variables.put(variableName, variable);
        }

        if (id % 2 == 0) {
            String variableName1 = "x" + (id - 1);
            Variable variable1 = new Variable((id - 1)*10, time, "init");
            this.variables.put(variableName1, variable1);

            String variableName2 = "x" + (id - 1 + 10);
            Variable variable2 = new Variable((id - 1 + 10)*10, time, "init");
            this.variables.put(variableName2, variable2);
        }
    }

    public Integer getId() {return this.id;}

    public Integer read(String varName) {
        return variables.get(varName).getValue();
    }

    public void write(String varName, Integer value, Long timestamp, String txId) {
        Variable original = this.variables.get(varName);
        original.setValue(value);
        original.setCommitTime(timestamp);
        original.setCommittedBy(txId);
        this.variables.put(varName, original);

        // get write lock
        Lock curLock = new Lock(txId, varName, LockType.WRITE);
        lockTable.put(varName, curLock);
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

        Lock lockInfo = this.lockTable.get(variableName);
        if (lockInfo.getLockType() == LockType.WRITE) {
            if (lockInfo.getTxId().equals(txId)) return true;
            // if not TODO implement
            return false;
        }
        // TODO implement
        return false;
    }

    public void showVariables() {
        for (String varName: this.variables.keySet()) {
            System.out.print(varName + ":" + this.variables.get(varName).getValue() + "  ");
        }
    }
}
