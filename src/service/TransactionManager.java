package service;
import service.Parser;

import java.util.List;

public class TransactionManager {

    private Integer timer = 0;

    public void runSimulation() {
        Parser parser = new Parser("data/test1.txt");
        List<List<String>> parsedCommands = parser.readAndParseCommands();

        for (List<String> commands: parsedCommands) {
            String command = commands.get(0);

            if (command.equals("begin")) {


            }



        }

    }


    private void dump() {


    }
}
