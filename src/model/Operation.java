package model;

import model.type.OperationType;

public class Operation {

    private OperationType operationType;
    private String varName;
    private Integer value;
    private String txId;
    private Long commitTime;

    public Operation(OperationType opType, String varName, Integer value, String txId, Long timestamp) {
        this.operationType = opType;
        this.varName = varName;
        this.value = value;
        this.txId = txId;
        this.commitTime = timestamp;
    }

    public OperationType getOperationType() {return operationType;}

    public String getVarName() {return varName;}

    public Integer getValue() {return value;}

    public String getTxId() {return txId;}


}
