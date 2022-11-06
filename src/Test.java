import static java.lang.System.out;
import service.Parser;
import service.TransactionManager;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        out.println("Hello World");
        Parser parser = new Parser("data/test1.txt");
        List<List<String>> res = parser.readAndParseCommands();
        for (List<String> re : res) {
            for (String s : re) {
                System.out.println(s);
            }
        }

        TransactionManager testManager = new TransactionManager();
        testManager.runSimulation();
    }
}
