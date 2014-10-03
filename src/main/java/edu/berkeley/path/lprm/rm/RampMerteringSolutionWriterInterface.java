package edu.berkeley.path.lprm.rm;

import java.io.OutputStream;

/**
 * Created by gomes on 9/10/2014.
 */
public interface RampMerteringSolutionWriterInterface {
    public void write_to_file(String filename, RampMeteringSolution rm);
    public void write_all_to_stream(OutputStream ps,RampMeteringSolution rm);
    public void write_to_stream(OutputStream ps,RampMeteringSolution rm,String varname,int numK);
}
