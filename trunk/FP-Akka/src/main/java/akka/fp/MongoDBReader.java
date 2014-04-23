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
import com.mongodb.*;
import com.mongodb.util.JSON;
import fp.util.SpecimenRecord;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 26.04.2013
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */

public class MongoDBReader extends UntypedActor {

    private final ActorRef listener;

    private String _mongodbHost = "fp2.acis.ufl.edu";
    private String _mongodbDB = "db";
    private String _mongodbCollection = "Occurrence";
    private String _mongodbQuery = "{year:\"1898\"}";

    private DBCursor cursor = null;
    private int cRecords = 0;
    private int cValidRecords = 0;
    private int totalRecords = 0;
    int invoc;

    private static final long serialVersionUID = 1L;


    public MongoDBReader(String mongodbHost, String mongodbDB, String mongodbCollection, String mongodbQuery, ActorRef listener) {
        this.listener = listener;
        if (mongodbHost != null) this._mongodbHost = mongodbHost;
        if (mongodbDB != null) this._mongodbDB = mongodbDB;
        if (mongodbCollection != null) this._mongodbCollection = mongodbCollection;
        if (mongodbQuery != null) this._mongodbQuery = mongodbQuery;
        invoc = 0;
    }

    @Override
    public void onReceive(Object o) throws Exception {
         /*
        Boolean test = true;
        while (test) {
            if (o instanceof String) {
                System.out.println("o = " + o.toString());
                test = false;
            }
        }
        */

        long start = System.currentTimeMillis();
        //System.out.println("Accessing MongoDB ...");
        try {
            //System.out.println(" Host: " + _mongodbHost);
            MongoClient mongoClient = new MongoClient(_mongodbHost);
            //for (String dbn : mongoClient.getDatabaseNames()) {
            //    System.out.println("   DB: " + dbn);
            //}
            //System.out.println(" DB: " + _mongodbDB);
            DB db = mongoClient.getDB(_mongodbDB);
            //for (String dbn : db.getCollectionNames()) {
            //    System.out.println("   Collection: " + dbn);
            //}
            //System.out.println(" Collection: " + _mongodbCollection);
            DBCollection coll = db.getCollection(_mongodbCollection);

            //BasicDBObject query = new BasicDBObject("_id", new ObjectId("511bf6a6e4b04106ea7e979b"));
            //DBCursor cursor = coll.find(query);
            totalRecords = 0;
            if (!_mongodbQuery.isEmpty()) {
                //System.out.println(" With query: "+ _mongodbQuery);
                Object query = JSON.parse(_mongodbQuery);
                cursor = coll.find((DBObject)query);
                totalRecords = cursor.count();
            } else {
                //System.out.println(" Without query");
                cursor = coll.find();
                totalRecords = cursor.count();
            }
            //System.out.println(" Records: "+ totalRecords);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        int seen;
        do {
            //System.out.println(" Starting ");
            seen = cursor.numSeen();
            try {
                readData();
            } catch (SocketException se) {
                System.out.println("Recovering...");
                MongoClient mongoClient = new MongoClient(_mongodbHost);
                DB db = mongoClient.getDB(_mongodbDB);
                DBCollection coll = db.getCollection(_mongodbCollection);
                if (!_mongodbQuery.isEmpty()) {
                    Object query = JSON.parse(_mongodbQuery);
                    cursor = coll.find((DBObject)query);
                } else {
                    cursor = coll.find();
                }
                cursor.skip(seen);
            }
            if (cRecords % 100 == 0) {
                //System.out.println("Read " + cValidRecords + " compatible from " + cRecords + " / " + totalRecords + " records.");
            }
        } while (cursor.hasNext());
        //System.out.println(" End ");
        //System.out.println("Read " + cValidRecords + " compatible from " + cRecords + " / " + totalRecords + " records.");

        if (cursor != null)
            cursor.close();
        //listener.tell(new Done(),getSelf());
        //listener.tell(new Broadcast(new Done()),getSelf());
        listener.tell(new Broadcast(PoisonPill.getInstance()),getSelf());
        //listener.tell(PoisonPill.getInstance(),getSelf());
        getContext().stop(getSelf());
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getName(),invoc,start,System.currentTimeMillis());
        invoc++;
    }

    private void readData() throws SocketException {
        if ( cursor == null || !cursor.hasNext()) return;

        DBObject dbo = cursor.next();
        cRecords++;

        SpecimenRecord out = new SpecimenRecord();
        for (String key : dbo.keySet()) {
            String fieldName = key;
            if (fieldName.startsWith("_")) fieldName = fieldName.substring(1);
            if (fieldName.contains(":")) fieldName = fieldName.replace(":","");

            Object o = dbo.get(key);
            out.put(fieldName,o.toString());
        }

        Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(out,this.getClass().getSimpleName(),invoc);

        ++cValidRecords;
        listener.tell(t,getSelf());
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped Reader");
        super.postStop();
    }
}


