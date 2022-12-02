/**
 * @author Yunjeon Lee, Kyu Doun Sim
 * @date Nov 27th - Dec 1st, 2022
 */

import service.Parser;
import service.TransactionManager;

import java.util.List;


public class Test {

    public static void main(String[] args) {
        TransactionManager txManager = new TransactionManager();
        txManager.runSimulation();

//        Parser parser = new Parser("data/test20.txt");
//        List<List<String>> res = parser.readAndParseCommands();
//        for (int i = 0; i < res.size(); i++) {
//            for (int j = 0; j < res.get(i).size(); j++) {
//                System.out.println(res.get(i).get(j));
//            }
//        }
    }
}
