package org.filteredpush.akka.actors.io;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import org.filteredpush.kuration.util.SpecimenRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.akka.data.ReadMore;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;
import org.filteredpush.akka.data.Curate;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

/**
 * Actor to read data from a CSV (tab delimited or comma delimited) input
 * file, and then send to a downstream consuming actor as specimen records.
 * Reads the first few records from the file, then expects the downstream
 * actor to send ReadMore messages to pull data from the file. 
 * 
 * @see org.filteredpush.akka.data.ReadMore
 * 
 * 
 * @author cobalt
 * @author Tianhong Song
 * @author mole
 * 
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 26.04.2013
 * Time: 16:41
 * 
 */
public class CSVReader extends UntypedActor {
	
	private static final Log log = LogFactory.getLog(CSVReader.class);
	
	private final ActorRef listener;

    private String _filePath = "/home/tianhong/test/data/1000.csv";
    
    private int cValidRecords = 0;
    String[] labelList;

    public char fieldDelimiter = '\t';
    public Character quote = '"';
    public boolean trimWhitespace = true;

    public Reader inputReader = null;
    //public String filePath = null;
    public String recordClass = null;
    public String[] headers = new String[]{};
    
    /**
     * Report reading records in this increment. 
     */
    private int reportSize = 1000;
    
    Iterator<CSVRecord> iterator;
    Object debug;


    int invoc;
    long start;

    public CSVReader(String filePath, ActorRef listener) {
        this.listener = listener;
        if (filePath != null) this._filePath = filePath;
        invoc = 0;
    }

    /**
	 * @return the reportSize, that is the number of records loaded
	 * before reporting on the progress of the load.
	 */
	public int getReportChunkSize() {
		return reportSize;
	}

	/**
	 * @param reportSize set the number of records that will be loaded
	 * before reporting on the progress of the load.
	 */
	public void setReportChunkSize(int reportSize) {
		this.reportSize = reportSize;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		// System.out.println(message.getClass().toString());

		if (message instanceof Curate) { 
			// startup 
			start = System.currentTimeMillis();

			if (inputReader == null) {
				if (_filePath != null) {

					Reader reader = null;

					try {
						reader = new FileReader(_filePath);
					} catch (FileNotFoundException e) {
						System.out.println("file not found");
						throw new FileNotFoundException("Input CSV file not found: " + _filePath);

					}

					inputReader = reader;
				} else {
					System.out.println("filePath is null");
				}
			}

			CSVFormat tabFormat = CSVFormat.newFormat(fieldDelimiter)
					.withIgnoreSurroundingSpaces(trimWhitespace)
					.withHeader(headers);
			//.withQuote(quote)

			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);


			debug = new Object();

			try{

				CSVParser csvParser = new CSVParser(inputReader, tabFormat);

				if (csvParser.getHeaderMap().size()==1)  {
					System.out.println("Read header line of input file as tab separated, found only one field, trying again as comma separated.");
					try {
						inputReader = new FileReader(_filePath);
					} catch (FileNotFoundException e) {
						System.out.println("file not found");
						throw new FileNotFoundException("Input CSV file not found: " + _filePath);

					}
					csvParser = new CSVParser(inputReader, csvFormat);
				}

				Map<String,Integer> csvHeader = csvParser.getHeaderMap();
				headers = new String[csvHeader.size()];
				int i = 0;
				for (String header: csvHeader.keySet()) {

		            
					headers[i++] = header;
				}

				iterator = csvParser.iterator();
				int initialLoad = 30;
				while (iterator.hasNext() && cValidRecords < initialLoad) {
					readRecord();
				}
                System.out.println("Read initial " + cValidRecords + " records.") ;
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
				System.out.println("1wrongcValidRecords = " + cValidRecords);
				e.printStackTrace();
			} catch (RuntimeException e) {
				System.out.println("2wrongcValidRecords = " + cValidRecords);
				System.out.println("debug = " + debug);
				e.printStackTrace();
			}
		} else if (message instanceof ReadMore) { 
			if (iterator.hasNext()) { 
				try { 
					readRecord();
				} catch (RuntimeException e) {
					System.out.println("3wrongcValidRecords = " + cValidRecords);
					System.out.println("debug = " + debug);
					e.printStackTrace();
				}
			}
		}

		if (iterator!=null && !iterator.hasNext()) { 
			listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
			getContext().stop(getSelf());
			//Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
		}
		invoc++;
	}

	private TermFactory termFactory = TermFactory.instance();	
	
	protected void readRecord() throws Exception { 
        CSVRecord csvRecord = iterator.next();
        debug = csvRecord;

        if (!csvRecord.isConsistent()) {
            throw new Exception("Wrong number of fields in record " + csvRecord.getRecordNumber());
        }

        //Map<String, String> record = _recordClass.newInstance();
        SpecimenRecord record = new SpecimenRecord();

        for (String header : headers) {
            String value = csvRecord.get(header);
			// Handle some conversion of alternative forms of headers as found in file to dwc expectations
			
            try { 
            	// Try to obtain standard DarwinCore simple name for term
                Term headerTerm = termFactory.findTerm(header);
                header = headerTerm.simpleName();
            } catch (IllegalArgumentException e) { 
            	log.debug(e.getMessage());
            }
            
            // TODO: Need to handle header elements in the form vocabulary:term
/*            
            if (header.contains(":")) { 
            	header  = header.substring(header.indexOf(":") + 1);
            }
            
            if (header.toLowerCase().equals("occurrenceid")) { 
            	header = "occurrenceId";
            }
            if (header.toLowerCase().equals("recordid")) { 
            	header = "recordId";
            }
*/            
            record.put(header, value);
        }
       // broadcast(record);

        Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);

        ++cValidRecords;
        listener.tell(t,getSelf());

        if(cValidRecords % reportSize == 0) { 
             System.out.println("Read " + reportSize + " records, total " + cValidRecords);
        }
	}

    @Override
    public void postStop() {
        System.out.println("Read a total of " + cValidRecords + " records.");
        System.out.println("Stopped Reader, processing remaining records.");
        //System.out.println(System.currentTimeMillis() - start);
        super.postStop();
    }

}