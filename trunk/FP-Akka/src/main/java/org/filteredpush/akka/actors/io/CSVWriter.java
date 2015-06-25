package org.filteredpush.akka.actors.io;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import org.filteredpush.akka.workflows.CSVWorkflow;
import org.filteredpush.kuration.util.SpecimenRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
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
    boolean taxonOnlyMode = false;
    //todo: make it more flexible

    public CSVWriter(String filePath, boolean taxonOnlyMode) {
        this.taxonOnlyMode = taxonOnlyMode;
        if (filePath != null) this._filePath = filePath;
        try {
            //System.out.println("filePath = " + filePath);
            csvPrinter = new CSVPrinter(new FileWriter(_filePath, true), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.NON_NUMERIC));
        } catch (IOException e) {
        	log.error(e.getMessage(),e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        invoc = 0;
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof TokenWithProv) {
           /* Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                    ((TokenWithProv) message).getActorCreated(),
                    ((TokenWithProv) message).getInvocCreated(),
                    this.getClass().getSimpleName(),
                    invoc,
                    ((TokenWithProv) message).getTimeCreated(),
                    System.currentTimeMillis());  */
        }
        if (message instanceof Token) {
            Object o = ((Token) message).getData();

            try {
                if (!headerWritten) {
                    //write header first
                    //use headers list to keep track of the order

                    Set<String> headerLabels;
                    if(!taxonOnlyMode){
                        for (String label  : ((SpecimenRecord)o).keySet()) {
                            csvPrinter.print(label);
                            headers.add(label);
                        }

                    }else{
                        Map<String, String> taxonHeaderLabels = constructTaxonOnlyLabels();
                        for (String label  : taxonHeaderLabels.keySet()) {
                            csvPrinter.print(taxonHeaderLabels.get(label));
                            headers.add(label);
                        }
                    }
                    csvPrinter.println();
                    headerWritten = true;
                }

                //write the values
                for (String header : headers){
                	String output = ((SpecimenRecord)o).get(header);
                	if (output!=null) { output = output.trim(); } 
                    csvPrinter.print(output);
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
        //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
        invoc++;
    }

    @Override
    public void postStop() {
        System.out.println("Stopped CSVWriter");
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

    public Map<String, String> constructTaxonOnlyLabels(){

        //key is the label in the record, value is the label in the csv file
    	// order is important, so using a LinkedHashMap.
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("dbpk", "dbpk");
        result.put("scientificName", "scientificName");
        result.put("scientificNameAuthorship", "authorship");
        result.put("taxonID", "guid");
        result.put(SpecimenRecord.SciName_Status_Label, "status");
        result.put(SpecimenRecord.Original_SciName_Label, "sciNameWas");
        result.put(SpecimenRecord.Original_Authorship_Label, "sciNameAuthorshipWas");
        result.put(SpecimenRecord.SciName_Comment_Label, "provenance");
        return result;
    }

}
