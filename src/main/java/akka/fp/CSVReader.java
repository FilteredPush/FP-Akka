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
import com.csvreader.CsvReader;
import com.mongodb.DBCursor;
import fp.util.SpecimenRecord;

import java.io.FileNotFoundException;
import java.io.IOException;

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

        /* use java csv library instead
        BufferedReader collectionFileReader = new BufferedReader(new FileReader(_filePath));
        String strLine = collectionFileReader.readLine();
        if (strLine!=null) { 
        	labelList = strLine.split(",");

     	do {
        		strLine = collectionFileReader.readLine();
        		//System.out.println(" Starting ");
        		readData(strLine);
        		//System.out.println(" End ");
        		if (strLine == null || cRecords % 1000 == 0) {
        			System.out.println("Read " + cValidRecords + " compatible from " + cRecords + " / " + totalRecords + " records.");
        		}
        	} while (strLine != null);

        }
        */

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

               // System.out.println("t.toString() = " + t.toString());

                ++cValidRecords;
                listener.tell(t,getSelf());
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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


