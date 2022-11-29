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

    // TODO operation queue, T1, T2, T1 은 어떻게 처리??
    private List<Operation> opQueue = new ArrayList<>();

    private Map<Integer, List<History>> failHistories = new HashMap<>(); // key: site, value: fail timestamp

    private DeadlockDetector detector = new DeadlockDetector();

    private void init() {
        for (int i = 1; i < 11; i++) {
            DataManager newSite = new DataManager(i, timer);
            this.sites.add(newSite);

            List<History> failHistory = new LinkedList<>();
            failHistories.put(i, failHistory);
            // timer++; TODO check time should be increased or not when sites are created
        }
    }

    public void runSimulation() {
        Parser parser = new Parser("data/test21.txt");
        List<List<String>> commands = parser.readAndParseCommands();
        init();

        for (List<String> command: commands) {
            String operation = command.get(0);
            // TODO implement deadlock
            if (detector.isDeadLock(sites, transactions)) {
                System.out.println("Deadlock detected. " + this.timer);
                Transaction victim = transactions.get(detector.getVictimAbortionTxID(transactions));
                victim.setIsAborted(true);
                System.out.println("[Timestamp: " + this.timer + "] " + victim.getTxId() + " is aborted.");
                processAbortedTx(detector.getVictimAbortionTxID(transactions));
                processOperations();
            }

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
                Integer siteId = Integer.valueOf(command.get(1));
                recover(siteId);
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

    private void fail(Integer siteId) {
        System.out.println("[Timestamp: " + this.timer + "] Site: " + siteId + " fails.");
        this.sites.get(siteId-1).setIsUp(false);
        this.sites.get(siteId-1).clearLockTable();

        for (String txId: transactions.keySet()) {
            Transaction transaction = transactions.get(txId);
            if (!readOnlyTx.contains(txId) && transaction.getVisitedSites().contains(siteId)) {
                transaction.setIsAborted(true);
            }
        }

        History failHistory = new History(siteId, "", null,  "", timer); // only timestamp is necessary
        List<History> siteFailHistories = failHistories.get(siteId);
        siteFailHistories.add(failHistory);
        failHistories.put(siteId, siteFailHistories);
    }

    private void recover(Integer siteId) {
        System.out.println("[Timestamp: " + this.timer + "] Site: " + siteId + " is recovered.");
        DataManager site = this.sites.get(siteId-1);
        site.setIsUp(true);
        site.setVariablesIsRead(false);
        // 이후에 write가 한번 발생하면 그 때는 read가 가능해진다. from test3.5 comment
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
            site.clearTxIdFromLockWaitingList(txId);
        }
        transactions.remove(txId);

        // remove operations that occurred by txId
        List<Operation> toBeRemoved = new ArrayList<>();
        for (Operation operation: opQueue) {
            if (operation.getTxId().equals(txId)) {
                toBeRemoved.add(operation);
            }
        }
        opQueue.removeAll(toBeRemoved);

       // opQueue.removeIf(singleOperation -> singleOperation.getTxId().equals(txId));
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
                }
            }
            else if (op.getOperationType().equals(OperationType.READ) && readOnlyTx.contains(op.getTxId())) {
                // read-only transaction
                Integer result = processReadOnly(op.getTxId(), op.getVarName());
                if (result == null) {
                    System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + op.getTxId() + " fails to read.");
                } else {
                    System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + op.getTxId() + " successfully reads the data, variable: " + op.getVarName() + ", value: " + result);
                    toBeRemoved.add(op);
                }
            } else if (op.getOperationType().equals(OperationType.READ)) {
                //TODO read transaction
                Integer result = processRead(op.getTxId(), op.getVarName());
                if (result == null) {
                    System.out.println("[Timestamp: " + this.timer + "] Read fails");
                } else {
                    System.out.println("[Timestamp: " + this.timer + "] Read Transaction " + op.getTxId() + " successfully reads the data, variable: " + op.getVarName() + ", value: " + result);
                    toBeRemoved.add(op);
                }
            }
        }

        opQueue.removeAll(toBeRemoved);
    }

    private Integer processRead(String txId, String variableName) {
        List<DataManager> targets = this.sites.stream()
                .filter(s -> s.isUp() && s.isExistVariable(variableName))
                .collect(Collectors.toList());

        if (targets.size() == 0) {
            System.out.println("[Timestamp: " + this.timer + "] Cannot process txId: " + txId + ", because sites that has " + variableName + " are unavailable.");
            return null;
        }

        Transaction currentTx = transactions.get(txId);
        Integer readResult = null;
        for (DataManager target: targets) {
            Integer resultFromSite = target.read(variableName, txId);
            // currentTx.addVisitedSites(target.getId()); // TODO read는 언제를 site visit으로 보는지
//            if (readResult != null) return readResult; // TODO read는 replicated의 경우 하나만 유효하면 바로 읽으면 되는지 그렇다면 visited는 어떻게 판별?
            if (resultFromSite != null) {
                currentTx.addVisitedSites(target.getId());
                readResult = resultFromSite;
            }
        }
        return readResult;
    }

    private Integer processReadOnly(String txId, String variableName) {
        Integer snapshot = null;
        Long readOnlyStartTime = transactions.get(txId).getTimestamp();

        for (DataManager site: sites) {
            if (!site.isUp() || !site.isExistVariable(variableName)) continue;
            snapshot = site.getSnapshot(variableName, readOnlyStartTime, failHistories.get(site.getId()));
            if (snapshot != null) return snapshot;
        }
        return snapshot;
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
                //target.updateWriteLockWaitingList(variableName, value,this.timer, txId);
                return null;
            }
        }

        // write variables
        Transaction currentTx = transactions.get(txId);
        for (DataManager target: targets) {
            target.write(variableName, value, this.timer, txId);
            currentTx.addVisitedSites(target.getId());
        }
        System.out.println("[Timestamp: " + this.timer + "] " + txId + " writes variable: " + variableName + "=" + value);
        return op;
    }
}
