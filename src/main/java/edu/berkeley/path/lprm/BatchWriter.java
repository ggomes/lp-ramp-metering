package edu.berkeley.path.lprm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class BatchWriter {

    private String path;
    private boolean append_to_file;
//    public WriteFile(String file_path){
//        path = file_path;
//    }

    public BatchWriter(String file_path, boolean append_value){
        path = file_path;
        append_to_file = append_value;
    }


    public void writeToFile(String textLine) throws IOException {
        FileWriter write = new FileWriter(path,append_to_file);
        PrintWriter prin_line = new PrintWriter(write);
        prin_line.print(textLine);
        prin_line.close();

    }

    public String getTableString ( int index, ArrayList<Double> ic, Double demand,Double ctm, double TVH,double TVM){
        String table_row = (Integer.toString(index) + "\t");
        for (Double density : ic) {
            table_row = table_row.concat(Double.toString(density) + "\t");
        }
        table_row = table_row + Double.toString(demand) + "\t" + Double.toString(ctm) + "\t" +
                Double.toString(TVH) + "\t" + Double.toString(TVM) + "\n";
        return table_row;

    }
}
