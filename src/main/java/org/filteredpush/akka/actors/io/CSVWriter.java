package org.filteredpush.akka.actors.io;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import org.filteredpush.kuration.util.SpecimenRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.akka.data.Prov;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

/**
 * Actor to write output to a CSV file.  
 * 
 * @author cobalt
 * @author mole
 * 
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
*/
public class CSVWriter extends UntypedActor {

	private static final Log log = LogFactory.getLog(CSVWriter.class);
	
    int cRecords = 0;
    int invoc = 0;
    private String _filePath = "/home/tianhong/test/data/test.csv";
    CSVPrinter csvPrinter;
    Boolean headerWritten = false;
    List<String> headers = new ArrayList<String>();
    //todo: make it more flexible

    public CSVWriter(String filePath) {
        if (filePath != null) this._filePath = filePath;
        try {
            //System.out.println("filePath = " + filePath);
            csvPrinter = new CSVPrinter(new FileWriter(_filePath, true), CSVFormat.DEFAULT);
        } catch (IOException e) {
        	log.error(e.getMessage(),e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        invoc = 0;
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof TokenWithProv) {
            Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                    ((TokenWithProv) message).getActorCreated(),
                    ((TokenWithProv) message).getInvocCreated(),
                    this.getClass().getSimpleName(),
                    invoc,
                    ((TokenWithProv) message).getTimeCreated(),
                    System.currentTimeMillis());
        }
        if (message instanceof Token) {
            Object o = ((Token) message).getData();

            try {
                if (!headerWritten) {
                    //write header first
                    //use headers list to keep track of the order
                    for (String label  : ((SpecimenRecord)o).keySet()) {

                        csvPrinter.print(label);
                        headers.add(label);
                    }
                    csvPrinter.println();
                }

                //write the values
                for (String header : headers){
                    //System.out.println("asfeafadf" + ((SpecimenRecord)o).get(header));
                    csvPrinter.print(((SpecimenRecord)o).get(header));
                }
                csvPrinter.println();
                csvPrinter.flush();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (++cRecords % 100 == 0) {
                //System.out.println("Wrote " + cRecords + " records.");
                log.debug("Wrote " + cRecords + " records.");
            }
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
        invoc++;
    }

    @Override
    public void postStop() {
        System.out.println("Stopped Display");
        //System.out.println("Wrote " + cRecords + " records.");
        try { 
            csvPrinter.close();
        } catch (NullPointerException e) { 
            System.out.println(e.getMessage());
        } catch (IOException e) {
        	System.out.println(e.getMessage());
        	log.error(e.getMessage(),e);
		}
        getContext().system().shutdown();
        super.postStop();
    }

}