package edu.berkeley.path.lprm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by gomes on 10/6/14.
 */
public class BatchWriter {

    private PrintWriter prn;

    public BatchWriter(String file_path) throws IOException {
        prn = new PrintWriter(new FileWriter(file_path));
    }

    public void write(String textLine) {
        prn.print(textLine);
    }

    public void close() {
        prn.close();
    }

    public String getTableString ( int index, ArrayList<Double> ic, Double demand,Double ctm, double TVH,double TVM,double cost){
        String table_row = (Integer.toString(index) + "\t");
        for (Double density : ic) {
            table_row = table_row.concat(Double.toString(density) + "\t");
        }
        table_row = table_row + Double.toString(demand) + "\t" + Double.toString(ctm) + "\t" +
                Double.toString(TVH) + "\t" + Double.toString(TVM) + "\t" + Double.toString(cost)+ "\n";
        return table_row;

    }
}
