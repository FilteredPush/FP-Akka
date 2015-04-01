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
package akka.fp;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import com.mongodb.DBCursor;
import fp.util.SpecimenRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
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

    public char fieldDelimiter = ',';
    public Character quote = '"';
    public boolean trimWhitespace = true;

    public Reader inputReader = null;
    public String filePath = null;
    public String recordClass = null;
    public String[] headers = new String[]{};


    int invoc;
    long start;

    private static final long serialVersionUID = 1L;


    public CSVReader(String filePath, ActorRef listener) {
        this.listener = listener;
        if (filePath != null) this._filePath = filePath;
        invoc = 0;
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
            if (filePath != null) {

                Reader reader = null;

                try {
                    reader = new FileReader(_filePath);
                } catch (FileNotFoundException e) {
                    throw new FileNotFoundException("Input CSV file not found: " + filePath);
                }

                inputReader = reader;
            } else {
                System.out.println("filePath is null");
            }
        }

        CSVFormat csvFormat = CSVFormat.newFormat(fieldDelimiter)
                .withIgnoreSurroundingSpaces(trimWhitespace)
                .withQuote(quote)
                .withHeader(headers);

        try (CSVParser csvParser = new CSVParser(inputReader, csvFormat)) {

            Map<String,Integer> csvHeader = csvParser.getHeaderMap();
            headers = new String[csvHeader.size()];
            int i = 0;
            for (String header: csvHeader.keySet()) {
                headers[i++] = header;
            }

            for (Iterator<CSVRecord> iterator = csvParser.iterator();iterator.hasNext();) {

                CSVRecord csvRecord = iterator.next();

                if (!csvRecord.isConsistent()) {
                    throw new Exception("Wrong number of fields in record " + csvRecord.getRecordNumber());
                }

                //Map<String, String> record = _recordClass.newInstance();
                SpecimenRecord record = new SpecimenRecord();

                for (String header : headers) {
                    String value = csvRecord.get(header);
                    record.put(header, value);
                }
               // broadcast(record);

                Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);

                ++cValidRecords;
                listener.tell(t,getSelf());
            }
        }

        //listener.tell(new Done(),getSelf());
        //listener.tell(new Broadcast(new Done()),getSel;
        //listener.tell(new Broadcast(Poiso
            //System.out.println(" DB: " + mongodbDB);nPill.getInstance()),getSelf());
        listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
        getContext().stop(getSelf());
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
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
        System.out.println("Stopped Reader");
        //System.out.println(System.currentTimeMillis() - start);
        super.postStop();
    }

}


