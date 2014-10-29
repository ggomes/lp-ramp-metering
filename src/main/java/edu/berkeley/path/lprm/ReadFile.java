package edu.berkeley.path.lprm;

import java.io.*;
import java.util.ArrayList;


/**
 * Created by negar on 10/16/14.
 */
public class ReadFile {

    private String path;
    public ReadFile(String file_path) throws FileNotFoundException {
        path = file_path;
    }

    public ArrayList<ArrayList<Double>> getTextContents() throws IOException {

        FileReader fr = new FileReader(path);
        BufferedReader textReader = new BufferedReader(fr);
        ArrayList<ArrayList<Double>> gridValues = new ArrayList<ArrayList<Double>>();
        int numOfLines = numberOfLines();
        String[] textData = new String[numOfLines];
        for (int i=0; i<numOfLines; i++){
            textData[i] = textReader.readLine();
            ArrayList<Double> gridPointsStates = new ArrayList<Double>();
            for (String grid : textData[i].split(",")) {
                gridPointsStates.add(Double.parseDouble(grid));
            }
            gridValues.add(gridPointsStates);
        }
        textReader.close();
        return  gridValues;
    }

    private int numberOfLines() throws IOException {
        FileReader file_to_read = new FileReader(path);
        BufferedReader bf = new BufferedReader(file_to_read);

        String aLine;
        int numberOfLines = 0;
        while ((aLine = bf.readLine())!=null)
            numberOfLines++;
        bf.close();
        return numberOfLines;
    }

}


