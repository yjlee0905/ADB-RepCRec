import static java.lang.System.out;
import service.Parser;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        out.println("Hello World");
        Parser parser = new Parser("/Users/yjeonlee/Desktop/ADB/FinalProject/data/test1.txt");
        List<List<String>> res = parser.readAndParseCommands();
        for (int i = 0; i < res.size(); i++) {
            for (int j = 0; j < res.get(i).size(); j++) {
                System.out.println(res.get(i).get(j));
            }
        }

    }
}
