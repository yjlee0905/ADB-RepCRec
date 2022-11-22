package service;

import model.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class DataManager {

    private Integer id;

    private Queue lockTable;

    private boolean isUp;

    private Map<String, Variable> variables = new HashMap<>();

    public DataManager(Integer id, Long time) {
        this.id = id;

        for (int i = 1; i <= 10; i++) {
            String variableName = "X" + i * 2;
            Variable variable = new Variable(i*2*10, time);
            this.variables.put(variableName, variable);
        }

        if (id % 2 == 0) {
            String variableName1 = "X" + (id - 1);
            Variable variable1 = new Variable((id - 1)*10, time);
            this.variables.put(variableName1, variable1);

            String variableName2 = "X" + (id - 1 + 10);
            Variable variable2 = new Variable((id - 1 + 10)*10, time);
            this.variables.put(variableName2, variable2);
        }
    }

    public Integer read(String varName) {
        return variables.get(varName).getValue();
    }

    public void write(String varName, Integer value) {
        // TODO implement
    }
}
