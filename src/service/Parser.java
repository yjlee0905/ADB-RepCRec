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

    public Parser(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * A function that will be called in TransactionManager's runSimulation function
     * to that will parse the commands in the input file as a 2D List of Strings
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