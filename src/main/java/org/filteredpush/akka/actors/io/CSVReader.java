/** A coactor that import collections represented in an XML file.
 *
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package org.filteredpush.akka.actors.io;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

import com.mongodb.DBCursor;

import org.filteredpush.kuration.util.SpecimenRecord;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.filteredpush.akka.data.Token;
import org.filteredpush.akka.data.TokenWithProv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 26.04.2013
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */

public class CSVReader extends UntypedActor {

    private final ActorRef listener;

    private String _filePath = "/home/tianhong/test/data/1000.csv";
    
    private DBCursor cursor = null;
    private int cRecords = 0;
    private int cValidRecords = 0;
    private int totalRecords = 0;
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
    private int chunkSize = 1000;


    int invoc;
    long start;

    private static final long serialVersionUID = 1L;


    public CSVReader(String filePath, ActorRef listener) {
        this.listener = listener;
        if (filePath != null) this._filePath = filePath;
        invoc = 0;
    }

    /**
	 * @return the chunkSize
	 */
	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * @param chunkSize the chunkSize to set
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
    public void onReceive(Object o) throws Exception {
        start = System.currentTimeMillis();

        /* use apache common CSV library instead
        try {
            CsvReader reader = new CsvReader(_filePath);
            reader.readHeaders();
            while (reader.readRecord())
            {
                cRecords++;
                SpecimenRecord out = new SpecimenRecord();
                //reader through the whole record
                for (String header : reader.getHeaders()){
                    out.put(header.replace("\"", ""), reader.get(header));
                    //System.out.println("header = " + header + ": " + reader.get(header));
                    //todo: may need validation steps here, some errors are ignored
                }
                Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(out,this.getClass().getSimpleName(),invoc);
                //System.out.println("out = " + out.prettyPrint());

                ++cValidRecords;
                listener.tell(t,getSelf());
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

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


        Object debug = new Object();

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

            Iterator<CSVRecord> iterator = csvParser.iterator();
            while (iterator.hasNext()) {

                CSVRecord csvRecord = iterator.next();
                debug = csvRecord;

                if (!csvRecord.isConsistent()) {
                    throw new Exception("Wrong number of fields in record " + csvRecord.getRecordNumber());
                }

                //Map<String, String> record = _recordClass.newInstance();
                SpecimenRecord record = new SpecimenRecord();

                for (String header : headers) {
                    String value = csvRecord.get(header);
                    // TODO: Need to handle header elements in the form vocabulary:term
                    if (header.contains(":")) { 
                    	header  = header.substring(header.indexOf(":") + 1);
                    }
                    record.put(header, value);
                }
               // broadcast(record);

                Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);

                ++cValidRecords;
                listener.tell(t,getSelf());

                if(cValidRecords % chunkSize == 0) { 
                     System.out.println("Read " + chunkSize + " records.");
                }
            }
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

        //listener.tell(new Done(),getSelf());
        //listener.tell(new Broadcast(new Done()),getSel;
        //listener.tell(new Broadcast(Poiso
            //System.out.println(" DB: " + mongodbDB);nPill.getInstance()),getSelf());
        // TODO: If we hit the end at less than chunkSize records assert that we are done reading the input.
        listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
        getContext().stop(getSelf());
        //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
        invoc++;
    }

        /* Fails here on lines that contain " to enclose strings that contain the comma delimiter. 
         strLine = "106497",,,,,"Parmeliaceae","Melanohalea ","Melanohalea subolivacea","(Nylander) O. Blanco, A. Crespo, P. K. Divakar, Esslinger, D. Hawksworth & Lumbsch",,,
[ERROR] [12/06/2013 10:55:36.856] [FpSystem-akka.actor.default-dispatcher-6] [akka://FpSystem/user/reader] 12
java.lang.ArrayIndexOutOfBoundsException: 12
	at akka.fp.CSVReader.readData(CSVReader.java:132)
         */


    @Override
    public void postStop() {
        System.out.println("Read a total of " + cValidRecords + " records.");
        System.out.println("Stopped Reader, processing these records.");
        //System.out.println(System.currentTimeMillis() - start);
        super.postStop();
    }

}


