package ex2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Exercise2 {
    public static void main(String[] args) {
        Exercise2 exercise2 = new Exercise2();

        for (String path : args) {
            exercise2.readFile(path);
        }
    }

    private int readFile(String fileName) {
        int lineNum = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            while (reader.readLine() != null) {
                lineNum++;
            }

            System.out.println("File "  + fileName + " line number is " + lineNum);
            reader.close();
        } catch (IOException e) {
            System.out.println("The file you input does not exist. Please input a valid file name");
        }

        return lineNum;
    }

}
