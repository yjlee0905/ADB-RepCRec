/**
 * @author Yunjeon Lee, Kyu Doun Sim
 * @date Nov 27th - Dec 1st, 2022
 */

package model;

import model.type.OperationType;

public class Operation {

    private OperationType operationType;
    private String varName;
    private Integer value;
    private String txId;
    private Long commitTime;

    /**
     * Constructor for the Operation class
     *
     * No other side effect other than allocating memory to construct
     * Operation
     *
     * @param opType
     * @param varName
     * @param value
     * @param txId
     * @param timestamp
     */
    public Operation(OperationType opType, String varName, Integer value, String txId, Long timestamp) {
        this.operationType = opType;
        this.varName = varName;
        this.value = value;
        this.txId = txId;
        this.commitTime = timestamp;
    }

    /**
     * Accessor for operationType variable
     *
     * No side effects.
     *
     * @return OperationType
     */
    public OperationType getOperationType() {return operationType;}

    /**
     * Accessor for varName variable
     *
     * No side effects
     *
     * @return String
     */
    public String getVarName() {return varName;}

    /**
     * Accessor for value variable
     *
     * No side effects
     *
     * @return Integer
     */
    public Integer getValue() {return value;}

    /**
     * Accessor for transaction ID
     *
     * No side effects
     *
     * @return String
     */
    public String getTxId() {return txId;}


}
