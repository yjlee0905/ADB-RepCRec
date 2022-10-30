package service;

import model.Variable;
import java.util.Map;
import java.util.Queue;

public class DataManager {

    private Queue lockTable;

    private boolean isUp;

    private Map<String, Variable> variables;

    public Integer read(String varName) {
        return variables.get(varName).getValue();
    }

    public void write(String varName, Integer value) {
        // TODO implement
    }
}
