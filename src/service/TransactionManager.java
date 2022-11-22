package service;
import model.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class TransactionManager {

    private Long timer = Long.valueOf(0);

    private List<DataManager> sites = new ArrayList<>();

    private Map<String, Transaction> transactions = new HashMap<>(); // key: T1, value: Transaction

    private void init() {
        for (int i = 1; i < 11; i++) {
            DataManager newSite = new DataManager(i, timer);
            this.sites.add(newSite);
            // timer++; TODO check time should be increased or not when sites are created
        }
    }

    public void runSimulation() {
        Parser parser = new Parser("data/test1.txt");
        List<List<String>> commands = parser.readAndParseCommands();
        init();

        for (List<String> command: commands) {
            String operation = command.get(0);

            if (operation.equals("begin")) {
                String txId = command.get(1);
                begin(txId);
            } else if (operation.equals("beginRO")) {

            } else if (operation.equals("R")) {

            } else if (operation.equals("W")) {
                String txId = command.get(1);
                String variableName = command.get(2);
                Integer value = Integer.valueOf(command.get(3));
                write(txId, variableName, value);
            } else if (operation.equals("end")) {

            } else if (operation.equals("fail")) {

            } else if (operation.equals("recover")) {

            } else if (operation.equals("dump")) {
                dump();
            } else {
                System.out.println("Invalid command: " + command + " detected.");
            }
            this.timer++;

        }

    }

    private void begin(String txId) {
        if (this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] " + txId + " has been already started.");
        }

        Transaction transaction = new Transaction(txId, this.timer);
        this.transactions.put(txId, transaction);
        System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " begins.");
    }

    private void write(String txId, String variableName, Integer value) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println("[Timestamp: " + this.timer + "] Transaction " + txId + " does not exists and cannot start write operation.");
            return;
        }

        // select write target sites
        List<DataManager> targets = this.sites.stream()
                .filter(s -> s.isUp() && s.isExistVariable(variableName))
                .collect(Collectors.toList());

        if (targets.size() == 0) {
            System.out.println("[Timestamp: " + this.timer + "] Cannot process txId: " + txId + ", because sites that has " + variableName + " are unavailable.");
        }

        // if there is a one site that cannot get write lock, wait
        for (DataManager target: targets) {
            if (!target.isWriteLockAvailable(txId, variableName)) {
                System.out.println("[Timestamp: " + this.timer + "] " + txId + " waits because of the write lock conflict in site: " + target.getId());
                return;
            }
        }

        // write variables
        System.out.println("[Timestamp: " + this.timer + "] " + txId + " writes variable: " + variableName + "=" + value);
        for (DataManager target: targets) {
            target.write(variableName, value, this.timer, txId);
        }
    }

    private void end(String txId) {
        if (!this.transactions.containsKey(txId)) {
            System.out.println(txId + " does not exist and cannot end this transaction.");
            return;
        }
        Transaction transaction = this.transactions.get(txId);

    }

    private void dump() {
        System.out.println("[Timestamp: " + this.timer + "] Dump");
        for (DataManager site: this.sites) {
            site.showVariables();
        }
    }
}
