package service;
import java.util.ArrayList;
import java.util.List;


public class TransactionManager {

    private Long timer = Long.valueOf(0);

    private List<DataManager> sites = new ArrayList<>();

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

            } else if (operation.equals("beginRO")) {

            } else if (operation.equals("R")) {

            } else if (operation.equals("W")) {

            } else if (operation.equals("end")) {

            } else if (operation.equals("fail")) {

            } else if (operation.equals("recover")) {

            } else if (operation.equals("dump")) {

            } else {
                System.out.println("Invalid command: " + command + " detected.");
            }
            timer++;

        }

    }
}
