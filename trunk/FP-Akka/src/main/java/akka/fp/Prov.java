package akka.fp;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 15.05.2013
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class Prov {

    private static PrintStream log = null;

    public static void init(String file) {
        try {
            log = new PrintStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static PrintStream log() {
        if (log == null) {
            log = System.err;
        }
        return log;
    }
}
