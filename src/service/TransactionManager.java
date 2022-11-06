package service;
import model.Transaction;

import java.util.ArrayList;
import java.util.List;


public class TransactionManager {

    private Integer timer = 0;
    private Integer transactionIDCounter = 0;

    private ArrayList<Transaction> transactionList = new ArrayList<>();
    private ArrayList<DataManager> dataManagerList =  new ArrayList<>();

    public void runSimulation() {
        Parser parser = new Parser("data/test1.txt");
        List<List<String>> parsedCommands = parser.readAndParseCommands();

        // iterate through the operations
        for (List<String> commands: parsedCommands) {
            String command = commands.get(0);

            if (command.equals("begin")) {
                System.out.println("begin command");
                transactionList.add(new Transaction(transactionIDCounter, commands.get(1), timer.longValue()));
                transactionIDCounter++;

            } else if (command.equals("beginRO")) {
                System.out.println("beginRO command");

            } else if (command.equals("end")) {
                System.out.println("end command");

            } else if (command.equals("R")) {
                System.out.println("R command");

            } else if (command.equals("W")) {
                System.out.println("W command");

            } else if (command.equals("fail")) {
                System.out.println("fail command");

            } else if (command.equals("recover")) {
                System.out.println("recover command");

            } else if (command.equals("dump")) {
                dump();
            }
            // time increase
            ++timer;
        }
    }

    private void dump() {
        System.out.println("dump function called.");
    }
}
