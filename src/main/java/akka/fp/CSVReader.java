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

import java.io.BufferedReader;
import java.io.FileReader;

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

        BufferedReader collectionFileReader = new BufferedReader(new FileReader(_filePath));
        String strLine = collectionFileReader.readLine();
        labelList = strLine.split(",");

       /* //handle the head line
        ArrayList<String> fieldNameArray = parseRecord(strLine, delimiter);
        for(int i=0;i<fieldNameArray.size();i++){
            String fieldName = fieldNameArray.get(i);
            if(fieldName.equals("")){
                throw new IllegalActionException(getClass().getName()+" failed for invalid csv file format: the field name field can't be empty.");
            }
            //replace colon by underscore since colon is not allowed to appear in the label name and also a field name of record type
            fieldName = fieldName.replaceAll(":", "_");

            fieldNameArray.set(i, fieldName);
        }
         */



        do {
            strLine = collectionFileReader.readLine();
            //System.out.println(" Starting ");
            readData(strLine);
            //System.out.println(" End ");
            if (strLine == null || cRecords % 1000 == 0) {
                System.out.println("Read " + cValidRecords + " compatible from " + cRecords + " / " + totalRecords + " records.");
            }
        } while (strLine != null);

        //listener.tell(new Done(),getSelf());
        //listener.tell(new Broadcast(new Done()),getSel;
        //listener.tell(new Broadcast(Poiso
            //System.out.println(" DB: " + mongodbDB);nPill.getInstance()),getSelf());
        listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
        getContext().stop(getSelf());
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
        invoc++;
    }

    private void readData(String strLine) {
        if ( strLine == null) return;
        cRecords++;

        SpecimenRecord out = new SpecimenRecord();
        String[] dataList = strLine.split(",");

        if (dataList.length > labelList.length) {
            System.out.println("strLine = " + strLine);
        }

        for(int i=0;i<dataList.length;i++){
            //if (fieldName.startsWith("_")) fieldName = fieldName.substring(1);
           // if (fieldName.contains(":")) fieldName = fieldName.replace(":","");

           // Object o = dbo.get(key);

            out.put(labelList[i].replace("\"", ""),dataList[i]);
        }

        //System.out.println("out = " + out);
        Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(out,this.getClass().getSimpleName(),invoc);

        ++cValidRecords;
        listener.tell(t,getSelf());
    }

    @Override
    public void postStop() {
        System.out.println("Stopped Reader");
        System.out.println(System.currentTimeMillis() - start);
        super.postStop();
    }

}


