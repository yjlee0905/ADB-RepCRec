package service;
import model.History;
import model.Operation;
import model.Variable;
import model.type.OperationType;
import model.Transaction;

import java.util.*;
import java.util.stream.Collectors;


public class TransactionManager {

    private Long timer = Long.valueOf(0);

    private List<DataManager> sites = new ArrayList<>();

    private Map<String, Transaction> transactions = new HashMap<>(); // key: T1, value: Transaction

    private Set<String> readOnlyTx = new HashSet<>();

    // operation queue, T1, T2, T1 은 어떻게 처리??
    private List<Operation> opQueue = new ArrayList<>();

    private Map<Integer, List<History>> failHistories = new HashMap<>(); // key: site, value: fail timestamp

    private DeadlockDetector detector = new DeadlockDetector();

    private void init() {
        for (int i = 1; i < 11; i++) {
            DataManager newSite = new DataManager(i, timer);
            this.sites.add(newSite);
            // timer++; TODO check time should be increased or not when sites are created
        }
    }

    public void runSimulation() {
        Parser parser = new Parser("data/test13.txt");
        List<List<String>> commands = parser.readAndParseCommands();
        init();

        for (List<String> command: commands) {
            String operation = command.get(0);

            // TODO implement deadlock
//            if (detector.isDeadLock(sites, transactions)) {
//                System.out.println("Deadlock detected. " + this.timer);
//            } else {
//                System.out.println("No deadlock. " + this.timer);
//            }

            if (operation.equals("begin")) {
                String txId = command.get(1);
                begin(txId);
            } else if (operation.equals("beginRO")) {
                String txId = command.get(1);
                beginRO(txId);
            } else if (operation.equals("R")) {
                String txId = command.get(1);
                String varName = command.get(2);
                read(txId, varName);
                processOperations();
            } else if (operation.equals("W")) {
                String txId = command.get(1);
                String variableName = command.get(2);
                Integer value = Integer.valueOf(command.get(3));
                write(txId, variableName, value);
                processOperations();
            } else if (operation.equals("end")) {
                String txId = command.get(1);
                end(txId);
                processOperations();
            } else if (operation.equals("fail")) {
                Integer siteId = Integer.valueOf(command.get(1));
                fail(siteId);
            } else if (operation.equals("recover")) {

            } else if (operation.equals("dump")) {
                dump();
            } else {
                System.out.println("Invalid command: " + command + " detected.");
            }
            this.timer++;

        }
        // for debugging
        dump();
    }

    private void begin(String txId) {
        if (this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " has been already started.");
        }

        Transaction transaction = new Transaction(txId, this.timer);
        this.transactions.put(txId, transaction);
        System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " begins.");
    }

    private void beginRO(String txId) {
        if (this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " has been already started.");
        }

        Transaction transaction = new Transaction(txId, this.timer);
        this.transactions.put(txId, transaction);
        this.readOnlyTx.add(txId);
        System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + txId + " begins.");
    }

    private void read(String txId, String variableName) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " does not exists and cannot start read operation.");
            return;
        }

        Operation op = new Operation(OperationType.READ, variableName, -1, txId, timer); // Read operation does not need value
        this.opQueue.add(op);
    }

    private void write(String txId, String variableName, Integer value) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " does not exists and cannot start write operation.");
            return;
        }

        Operation op = new Operation(OperationType.WRITE, variableName, value, txId, timer);
        this.opQueue.add(op);
    }

    private void end(String txId) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " does not exist and cannot end this transaction.");
            return;
        }

        Transaction transaction = this.transactions.get(txId);
        if (transaction.isAborted()) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " is aborted.");
            processAbortedTx(txId);
        } else { // commit
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " is committed.");
            processCommitTx(txId);
        }
    }

    private void fail(Integer siteId){
        System.out.println("[Timestamp: " + this.timer + "] Site: " + siteId + " fails.");
        this.sites.get(siteId-1).setIsUp(false);
        this.sites.get(siteId-1).clearLockTable();

        History failHistory = new History(siteId, "", "", timer); // only timestamp is necessary
        List<History> siteFailHistories = failHistories.get(siteId);
        siteFailHistories.add(failHistory);
        failHistories.put(siteId, siteFailHistories);
    }

    private void dump() {
        System.out.println("[Timestamp: " + this.timer + "] Dump");
        for (DataManager site: this.sites) {
            System.out.print("Site " + site.getId() + " - ");
            site.showVariables();
            System.out.println();
        }
    }

    /*
    * Transaction aborted case
    * 1) deadlock detection
    * 2) site fails
    * */
    private void processAbortedTx(String txId) {
        for (DataManager site: sites) {
            site.clearTxId(txId);
        }
        transactions.remove(txId);
        // temp 초기화?
    }

    private void processCommitTx(String txId) {
        // TODO split into temp and commit value?
        for (DataManager site: sites) {
            site.processCommit(txId, timer);
        }
        transactions.remove(txId);
    }

    private void processOperations() {
        List<Operation> toBeRemoved = new ArrayList<>();

        for (Operation op: opQueue) {
            if (op.getOperationType().equals(OperationType.WRITE)) {
                Operation result = processWrite(op.getTxId(), op.getVarName(), op.getValue(), op);
                if (result != null) {
                    toBeRemoved.add(op);
//                    opQueue.remove(op);
//                    break; // TODO check
                }
            }
            else if (op.getOperationType().equals(OperationType.READ) && readOnlyTx.contains(op.getTxId())) {
                // read-only transaction
                Variable result = processReadOnly(op.getTxId(), op.getVarName());
                if (result == null) {
                    System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + op.getTxId() + " fails to read.");
                } else {
                    System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + op.getTxId() + " successfully reads the data, variable: " + op.getVarName() + ", value: " + result.getValue());
                    toBeRemoved.add(op);
//                    opQueue.remove(op);
                }
            } else if (op.getOperationType().equals(OperationType.READ)) {
                //TODO read transaction
            }
        }

        opQueue.removeAll(toBeRemoved);
    }

    private Variable processReadOnly(String txId, String variableName) {
        Long readOnlyStartTime = transactions.get(txId).getTimestamp();

        for (DataManager site: sites) {
            if (!site.isUp() || !site.isExistVariable(variableName)) continue;
            return site.getSnapshot(variableName, readOnlyStartTime, failHistories.get(site.getId()));
        }
        return null;
    }

    private Operation processWrite(String txId, String variableName, Integer value, Operation op) {
        // select write target sites
        List<DataManager> targets = this.sites.stream()
                .filter(s -> s.isUp() && s.isExistVariable(variableName))
                .collect(Collectors.toList());

        if (targets.size() == 0) {
            System.out.println("[Timestamp: " + this.timer + "] Cannot process txId: " + txId + ", because sites that has " + variableName + " are unavailable.");
            return null;
        }

        // if there is a one site that cannot get write lock, wait
        for (DataManager target: targets) {
            if (!target.isWriteLockAvailable(txId, variableName)) {
                System.out.println("[Timestamp: " + this.timer + "] " + txId + " waits because of the write lock conflict in site: " + target.getId());
                return null;
            }
        }

        // write variables
        for (DataManager target: targets) {
            target.write(variableName, value, this.timer, txId);
        }
        System.out.println("[Timestamp: " + this.timer + "] " + txId + " writes variable: " + variableName + "=" + value);
        return op;
    }
}
