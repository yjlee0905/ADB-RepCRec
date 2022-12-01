package service;
import model.History;
import model.Operation;
import model.type.OperationType;
import model.Transaction;

import java.util.*;
import java.util.stream.Collectors;


public class TransactionManager {

    private Long timer = Long.valueOf(0);

    private List<DataManager> sites = new ArrayList<>();

    private Map<String, Transaction> transactions = new HashMap<>(); // key: T1, value: Transaction

    private Set<String> readOnlyTx = new HashSet<>();

    private List<Operation> opQueue = new ArrayList<>();

    private Map<Integer, List<History>> failHistories = new HashMap<>(); // key: site, value: fail timestamp

    private DeadlockDetector detector = new DeadlockDetector();

    /**
     * initialize the application
     *  - create 10 sites with initialization
     *  - create keys for failHistories (key: site, value: site fail time)
     * no param and return
     * */
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
            if (detector.isDeadLock(sites, transactions)) {
                Transaction victim = transactions.get(detector.getVictimAbortionTxID(transactions));
                victim.setIsAborted(true);
                System.out.println("[Timestamp: " + this.timer + "] Deadlock detected and Victim " + victim.getTxId() + " is aborted.");
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

    /**
     * When the transaction begins, add to transactions map (key: transaction id, value: class Transaction)
     * @param txId String
     * @return no return
     * */
    private void begin(String txId) {
        if (this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " has been already started.");
        }

        Transaction transaction = new Transaction(txId, this.timer);
        this.transactions.put(txId, transaction);
        System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " begins.");
    }

    /**
     * When the read-only transaction begins, add to transactions map (key: transaction id, value class Transaction)
     * Add to read-only transaction set
     * @param txId String
     * @return no return
     * */
    private void beginRO(String txId) {
        if (this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " has been already started.");
        }

        Transaction transaction = new Transaction(txId, this.timer);
        this.transactions.put(txId, transaction);
        this.readOnlyTx.add(txId);
        System.out.println("[Timestamp: " + this.timer + "] Read-only Transaction " + txId + " begins.");
    }

    /**
     * When the transaction requires READ operation, add to operation queue
     * @params txId String, variableName String
     * @return no return
     * */
    private void read(String txId, String variableName) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " does not exists and cannot start read operation.");
            return;
        }

        Operation op = new Operation(OperationType.READ, variableName, -1, txId, timer); // Read operation does not need value
        this.opQueue.add(op);
    }

    /**
     * When the transaction requires WRITE operation, add to operation queue
     * @params txId String, variableName String, value Integer
     * @return no return
     * */
    private void write(String txId, String variableName, Integer value) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " does not exists and cannot start write operation.");
            return;
        }

        Operation op = new Operation(OperationType.WRITE, variableName, value, txId, timer);
        this.opQueue.add(op);
    }

    /**
     * decides the transaction whether it should be aborted or committed and process the transaction
     * @param txId String
     * @return no return
     * */
    private void end(String txId) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " does not exist and cannot end this transaction.");
            return;
        }

        Transaction transaction = this.transactions.get(txId);
        if (transaction.isAborted()) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " is aborted, because site is failed.");
            processAbortedTx(txId);
        } else { // commit
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " is committed.");
            processCommitTx(txId);
        }
    }

    /**
     * When the site: siteId fails, set isUp attribute in DataManager(site) to false, and clear the lock table (lock information disappears when site is failed)
     * If there is a transaction that visited the site: siteId before it fails, that transaction should be aborted.
     * Add this fail event to fail histories.
     * @param siteId Integer
     * @return no return
     * */
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

    /**
     * When the site: siteId recovers, set isUp attribute in DataManager(site) to true,
     * and set isRead to false at replicated variables in the site.
     * @param siteId Integer
     * @return no return
     * */
    private void recover(Integer siteId) {
        System.out.println("[Timestamp: " + this.timer + "] Site: " + siteId + " is recovered.");
        DataManager site = this.sites.get(siteId-1);
        site.setIsUp(true);
        site.setVariablesIsRead(false);
        // from test 3.5 comment, this values can be readable after write operation occurs.
    }
    /**
     * show the values of all the variables in all the sites
     * no parameters and no returns
     * */
    private void dump() {
        System.out.println("[Timestamp: " + this.timer + "] Dump");
        for (DataManager site: this.sites) {
            System.out.print("Site " + site.getId() + " - ");
            site.showVariables();
            System.out.println();
        }
    }

    /**
     * When transaction is aborted, we should release lock from lock table and lock waiting list with txId.
     * And remove the transaction from transactions map and operation queue.
     * Transaction aborted case
     * 1) deadlock detection
     * 2) site fails
     * @param txId String
     * @return no return
     * */
    private void processAbortedTx(String txId) {
        for (DataManager site: sites) {
            site.clearTxId(txId, timer);
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
    }

    /**
     * When transaction is committed, temporary values written my WRITE request (which is no committed yet) should be written
     * and should release lock from lock table.
     * And remove the transaction from transaction map
     * @param txId String
     * @return no return
     * */
    private void processCommitTx(String txId) {
        for (DataManager site: sites) {
            site.processCommit(txId, timer);
        }
        transactions.remove(txId);
    }

    /**
     * process the operations in the operation queue.
     * operation type: read, read-only, write
     * no param and no return
     * */
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
                System.out.print("[Timestamp: " + this.timer + "] ");
                Integer result = processReadOnly(op.getTxId(), op.getVarName());
                if (result == null) {
                    System.out.println("Read-only Transaction " + op.getTxId() + " fails to read and is aborted.");
                    // TODO abort?
                } else {
                    System.out.println("Read-only Transaction " + op.getTxId() + " successfully reads the data, variable: " + op.getVarName() + ", value: " + result);
                    toBeRemoved.add(op);
                }
            } else if (op.getOperationType().equals(OperationType.READ)) {
                System.out.print("[Timestamp: " + this.timer + "] ");
                Integer result = processRead(op.getTxId(), op.getVarName());
                if (result == null) {
                    System.out.println("Read fails");
                } else {
                    System.out.println("Read Transaction " + op.getTxId() + " successfully reads the data, variable: " + op.getVarName() + ", value: " + result);
                    toBeRemoved.add(op);
                }
            }
        }

        opQueue.removeAll(toBeRemoved);
    }

    /**
     * For the sites that are up and has the variable, we do the read operation.
     * When we can read the value of variable from the sites returns the value,
     * but if not return null.
     * @params txId String, variableName String
     * @return Integer, the value of variableName read from available site
     * */
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
        Integer siteId = null;
        for (DataManager target: targets) {
            Integer resultFromSite = target.read(variableName, txId, timer);
            // currentTx.addVisitedSites(target.getId()); // TODO read는 언제를 site visit으로 보는지
//            if (readResult != null) return readResult; // TODO read는 replicated의 경우 하나만 유효하면 바로 읽으면 되는지 그렇다면 visited는 어떻게 판별?
            if (resultFromSite != null) {
                currentTx.addSitesVisited(target.getId());
                readResult = resultFromSite;
                if (siteId == null) siteId = target.getId();
            }
        }
        if (siteId != null) {
            System.out.print("From site: " + siteId + ", ");
        }
        return readResult;
    }

    /**
     * For the sites that are up and has the variable, we do the read-only operation.
     * Since read-only transaction does not need lock,
     * if variable is not replicated, return the last value committed before starting of the read-only transaction value directly,
     * if variable is replicated, if there is no fail history between last commit and starting of the read-only transaction, return the value
     * @params txId String, variableName String
     * @return Integer
     * */
    private Integer processReadOnly(String txId, String variableName) {
        Integer snapshot = null;
        Long readOnlyStartTime = transactions.get(txId).getTimestamp();

        for (DataManager site: sites) {
            if (!site.isUp() || !site.isExistVariable(variableName)) continue;
            snapshot = site.getSnapshot(variableName, readOnlyStartTime, failHistories.get(site.getId()));
            if (snapshot != null) {
                System.out.print("From site: " + site.getId() + ", ");
                return snapshot;
            }
        }
        return snapshot;
    }

    /**
     * For the sites that are up and has the variable, we do the write operation.
     * Within the WRITE operation, if there is at least 1 site that cannot get the write lock, the operation should wait for the lock.
     * When all the sites are available of holding the WRITE lock, we can write to temporary value(because the value is not committed yet) and add visited sites for the transaction.
     * @params txId String, variableName String, value Integer, op Operation
     * @return Operation if write operation is completed, if not return null
     * */
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
            if (!target.isWriteLockAvailable(txId, variableName, timer)) {
                System.out.println("[Timestamp: " + this.timer + "] " + txId + " waits because of the write lock conflict in site: " + target.getId());
                //target.updateWriteLockWaitingList(variableName, value,this.timer, txId);
                return null;
            }
        }

        // write variables
        Transaction currentTx = transactions.get(txId);
        String sites = "";
        for (DataManager target: targets) {
            target.write(variableName, value, this.timer, txId);
            sites += target.getId() + ", ";
            currentTx.addSitesVisited(target.getId());
        }
        System.out.println("[Timestamp: " + this.timer + "] " + txId + " writes variable: " + variableName + "=" + value + " to site: " + sites);
        return op;
    }
}
