package akka.fp;

import java.io.ByteArrayOutputStream;
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

    private static PrintStream log = System.err;

    public static void init(String file) {
        try {
            if (file != null)
                log = new PrintStream(file);
            else
                log = new PrintStream(new ByteArrayOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static PrintStream log() {
        return log;
    }
}
