/**
 * @author Yunjeon Lee, Kyu Doun Sim
 * @date Oct 29th - Oct 29th, 2022
 */

package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Parser {

    private final String fileName;

    /**
     * Constructor for Parser. It takes in a fileName and
     * updates the fileName variable as is.
     *
     * No side effects.
     *
     * @param fileName
     */
    public Parser(String fileName) {
        this.fileName = fileName;
    }

    /**
     * A function that will be called in TransactionManager's runSimulation function
     * to that will parse the commands in the input file as a 2D List of Strings
     *
     * No side effects, but it will print on the console whenver a file does not exist.
     *
     * @return List<List<String>> which contains the type of operation, variable name or value
     * depending on the operation parsed from the input file
     *
     */
    public List<List<String>> readAndParseCommands() {
        List<List<String>> parsedCommands = new ArrayList();
        File inputFile = new File(this.fileName);
        try {
            Scanner reader = new Scanner(inputFile);
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();

                if (line.length() == 0 || line.startsWith("//")) continue;
                if (line.indexOf("//") > 0) {
                    line = line.substring(0, line.indexOf("//"));
                }

                List<String> parsedLine = parseLine(line);
                parsedCommands.add(parsedLine);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File name: " + this.fileName + " cannot be found.");
        }
        return parsedCommands;
    }


    /**
     * A function that will parse a single line and return a list of string
     * that contain information relevant to the operation.
     *
     * No side effects.
     *
     * @param line A String of line
     * @return a List of Strings that will only
     *         contain relevant information for each Operation
     */
    private List<String> parseLine(String line) {
        int openIdx = line.indexOf('(');
        int closeIdx = line.indexOf(')');

        String command = line.substring(0, openIdx);
        String params = line.substring(openIdx+1, closeIdx);
        String[] splittedParams = params.split(",");
        ArrayList<String> converted = Arrays.stream(splittedParams)
                        .map(s -> s.trim())
                        .collect(Collectors.toCollection(ArrayList::new));

        List<String> results = new ArrayList<>();
        results.add(command);
        results.addAll(converted);
        return results;
    }
}